package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.util.ExplicitTimeUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.BindingFactsQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.BindingFactsResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BindingFactsParamException;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.BindingFactsRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsDeliveryPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsOrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsPartyPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 绑车场景订单关联与交付事实只读查询（OTD-VSO-DSN-CR-017）
 * <p>
 * 设计边界：
 * <ul>
 *   <li>只读：不修改订单、主体、VIN、交付记录、预约、时间线、Outbox 或订单状态；</li>
 *   <li>不调用 TSP、不依赖 iov-tsp-api、不写绑定/激活关系，不返回 canBind/isOwner 结论；</li>
 *   <li>同一只读事务内以可重复读取快照读取订单、主体与交付事实，避免分段读取跨交付提交；</li>
 *   <li>技术失败不捕获、不降级成 NO_MATCH 空成功，由框架既有技术失败语义返回。</li>
 * </ul>
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BindingFactsQueryAppService {

    /**
     * 查询契约版本（不是数据库行版本）
     */
    public static final int SCHEMA_VERSION = 1;

    /**
     * 构成账号关联的角色白名单：下单人、购车人、交付联系人。
     * 发票/上牌/紧急联系人等其他角色不构成绑车关联候选（OTD-VSO-DSN-CR-017 评审确认）。
     */
    public static final List<String> MATCHED_ROLES = List.of("order_user", "buyer", "delivery_contact");

    /**
     * 候选探测上限：读到 2 条即可判定歧义，不截断成唯一结果
     */
    public static final int CANDIDATE_LIMIT = 3;

    /**
     * 交付完成状态值（交付记录 delivery_status）
     */
    private static final String DELIVERY_STATUS_DELIVERED = "delivered";

    /**
     * 历史“已激活”状态值（OTD-VSO-DSN-CR-016 已从状态机移除，不得按已交付推定）
     */
    private static final int LEGACY_ACTIVATED_STATE = 700;

    /**
     * 统一 VIN 规则：17 位，不含 I/O/Q
     */
    private static final Pattern VIN_PATTERN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");

    /**
     * 账号标识长度上限（与 vso_order_party.user_id 列宽一致）
     */
    private static final int ACCOUNT_ID_MAX_LENGTH = 64;

    /**
     * 显式交付前状态集合（禁止用 orderState &lt; 650 / &gt;= 650 推断）
     */
    private static final Set<OrderState> PRE_DELIVERY_STATES = Collections.unmodifiableSet(EnumSet.of(
            OrderState.WISHLIST,
            OrderState.EARNEST_MONEY_UNPAID,
            OrderState.EARNEST_MONEY_PAID,
            OrderState.DOWN_PAYMENT_UNPAID,
            OrderState.DOWN_PAYMENT_PAID,
            OrderState.PENDING_AUDIT,
            OrderState.AUDIT_PASSED,
            OrderState.AUDIT_REJECTED,
            OrderState.ARRANGE_PRODUCTION,
            OrderState.ALLOCATION_VEHICLE,
            OrderState.APPLY_TRANSPORT,
            OrderState.PREPARE_TRANSPORT,
            OrderState.TRANSPORTING,
            OrderState.PREPARE_DELIVER,
            OrderState.FINAL_PAYMENT_PAID,
            OrderState.INVOICED));

    /**
     * 已交付口径集合：650 与 900 使用同一已交付口径
     */
    private static final Set<OrderState> DELIVERED_STATES = Collections.unmodifiableSet(EnumSet.of(
            OrderState.DELIVERED,
            OrderState.COMPLETED));

    /**
     * 无法安全解释的已知终态集合（退车/退款/取消/失效/关闭）：已知矛盾按 INCONSISTENT 表达
     */
    private static final Set<OrderState> CONFLICT_STATES = Collections.unmodifiableSet(EnumSet.of(
            OrderState.RETURN_APPLY,
            OrderState.RETURN_STORAGE,
            OrderState.RETURN_AUDIT,
            OrderState.RETURN_COMPLETED,
            OrderState.REFUND_APPLY,
            OrderState.REFUND_COMPLETE,
            OrderState.CANCEL,
            OrderState.EXPIRED,
            OrderState.CLOSED));

    private final BindingFactsRepository bindingFactsRepository;
    private final BusinessTimeZone businessTimeZone;

    /**
     * 查询账号与 VIN 的订单关联及交付事实（只读、可重复调用）
     *
     * @param query 账号与 VIN 查询条件
     * @return 最小化事实与核验状态
     */
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public BindingFactsResult query(BindingFactsQuery query) {
        String accountId = requireAccountId(query == null ? null : query.getAccountId());
        String vin = normalizeVin(query == null ? null : query.getVin());
        Instant checkedAt = ExplicitTimeUtil.nowInstant();

        List<BindingFactsOrderPo> candidates =
                bindingFactsRepository.findCandidateOrders(accountId, vin, MATCHED_ROLES, CANDIDATE_LIMIT);
        if (candidates == null || candidates.isEmpty()) {
            return emptyResult(checkedAt, BindingLookupStatus.NO_MATCH, BindingReasonCode.NO_ACCOUNT_VIN_MATCH);
        }
        if (candidates.size() > 1) {
            return emptyResult(checkedAt, BindingLookupStatus.AMBIGUOUS, BindingReasonCode.MULTIPLE_ORDER_MATCHES);
        }

        BindingFactsOrderPo order = candidates.get(0);
        List<BindingFactsPartyPo> parties = bindingFactsRepository.findMatchedParties(
                accountId, List.of(order.getOrderId()), MATCHED_ROLES);
        if (parties == null || parties.isEmpty()) {
            return emptyResult(checkedAt, BindingLookupStatus.NO_MATCH, BindingReasonCode.NO_ACCOUNT_VIN_MATCH);
        }
        List<BindingFactsDeliveryPo> deliveryRecords =
                bindingFactsRepository.findDeliveryRecords(List.of(order.getOrderId()));

        Classification classification = classify(order, vin, deliveryRecords);
        if (classification.lookupStatus() != BindingLookupStatus.MATCHED) {
            return emptyResult(checkedAt, classification.lookupStatus(), classification.reasonCode());
        }
        return BindingFactsResult.builder()
                .schemaVersion(SCHEMA_VERSION)
                .checkedAt(checkedAt)
                .lookupStatus(BindingLookupStatus.MATCHED)
                .reasonCode(null)
                .facts(List.of(buildFact(order, vin, parties, classification)))
                .build();
    }

    /**
     * 状态与交付证据判定（OTD-VSO-DSN-CR-017 §5.2 判定矩阵）
     */
    private Classification classify(BindingFactsOrderPo order, String vin, List<BindingFactsDeliveryPo> records) {
        Integer stateValue = order.getOrderState();
        OrderState state = stateValue == null ? null : OrderState.fromValue(stateValue);
        boolean orderVinMatched = vin.equals(normalizeStoredVin(order.getVehicleVin()));
        List<BindingFactsDeliveryPo> validRecords = records == null ? List.of() : records;

        if (state == null) {
            // 历史 700 与未知枚举：未迁移/未知为事实不足，不得按已交付推定
            if (stateValue != null && stateValue == LEGACY_ACTIVATED_STATE) {
                log.debug("绑车事实查询命中历史已激活状态[700]，按事实不足返回");
            }
            return Classification.of(BindingLookupStatus.INSUFFICIENT, BindingReasonCode.UNSUPPORTED_LEGACY_STATE);
        }
        if (CONFLICT_STATES.contains(state)) {
            return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.STATE_EVIDENCE_CONFLICT);
        }
        if (DELIVERED_STATES.contains(state)) {
            if (!orderVinMatched) {
                return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.VIN_EVIDENCE_MISMATCH);
            }
            if (validRecords.isEmpty()) {
                return Classification.of(BindingLookupStatus.INSUFFICIENT, BindingReasonCode.MISSING_DELIVERY_EVIDENCE);
            }
            if (validRecords.size() > 1) {
                return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.STATE_EVIDENCE_CONFLICT);
            }
            BindingFactsDeliveryPo record = validRecords.get(0);
            if (record.getActualDeliveryTime() == null
                    || !DELIVERY_STATUS_DELIVERED.equalsIgnoreCase(record.getDeliveryStatus())) {
                return Classification.of(BindingLookupStatus.INSUFFICIENT, BindingReasonCode.MISSING_DELIVERY_EVIDENCE);
            }
            if (!vin.equals(normalizeStoredVin(record.getVehicleVin()))) {
                return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.VIN_EVIDENCE_MISMATCH);
            }
            return Classification.matched(BindingDeliveryStatus.DELIVERED, record);
        }
        if (PRE_DELIVERY_STATES.contains(state)) {
            if (!orderVinMatched) {
                return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.VIN_EVIDENCE_MISMATCH);
            }
            if (!validRecords.isEmpty()) {
                // 交付前状态却存在有效交付记录：状态与交付事实冲突，不挑任意一侧
                return Classification.of(BindingLookupStatus.INCONSISTENT, BindingReasonCode.STATE_EVIDENCE_CONFLICT);
            }
            return Classification.matched(BindingDeliveryStatus.NOT_DELIVERED, null);
        }
        // 兜底：当前无法安全解释的状态（新增/未治理枚举）
        return Classification.of(BindingLookupStatus.INSUFFICIENT, BindingReasonCode.UNSUPPORTED_LEGACY_STATE);
    }

    private BindingFactsResult.Fact buildFact(BindingFactsOrderPo order, String vin,
                                              List<BindingFactsPartyPo> parties, Classification classification) {
        Set<String> roles = new LinkedHashSet<>();
        List<BindingFactsResult.EvidenceRef> evidenceRefs = new ArrayList<>();
        evidenceRefs.add(BindingFactsResult.EvidenceRef.builder()
                .type(BindingEvidenceType.ORDER)
                .id(order.getOrderId())
                .version(order.getRowVersion())
                .build());

        Set<String> seenPartyIds = new LinkedHashSet<>();
        for (BindingFactsPartyPo party : parties) {
            if (party.getPartyRole() != null) {
                roles.add(party.getPartyRole());
            }
            if (party.getPartyId() != null && seenPartyIds.add(party.getPartyId())) {
                evidenceRefs.add(BindingFactsResult.EvidenceRef.builder()
                        .type(BindingEvidenceType.PARTY)
                        .id(party.getPartyId())
                        .version(party.getRowVersion())
                        .build());
            }
        }

        BindingFactsDeliveryPo record = classification.deliveryRecord();
        if (record != null) {
            evidenceRefs.add(BindingFactsResult.EvidenceRef.builder()
                    .type(BindingEvidenceType.DELIVERY)
                    .id(record.getDeliveryRecordId())
                    .version(record.getRowVersion())
                    .build());
        }

        return BindingFactsResult.Fact.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .orderState(order.getOrderState())
                .vin(vin)
                .matchedRoles(List.copyOf(roles))
                .deliveryStatus(classification.deliveryStatus())
                .deliveryRecordId(record == null ? null : record.getDeliveryRecordId())
                .actualDeliveryTime(record == null ? null : businessTimeZone.toInstant(record.getActualDeliveryTime()))
                .evidenceRefs(evidenceRefs)
                .build();
    }

    private BindingFactsResult emptyResult(Instant checkedAt, BindingLookupStatus lookupStatus,
                                           BindingReasonCode reasonCode) {
        return BindingFactsResult.builder()
                .schemaVersion(SCHEMA_VERSION)
                .checkedAt(checkedAt)
                .lookupStatus(lookupStatus)
                .reasonCode(reasonCode)
                .facts(List.of())
                .build();
    }

    /**
     * 账号断言规范化：缺失/超长按参数错误处理，不做姓名/手机号/证件推断，不用前端请求头替代
     */
    private String requireAccountId(String accountId) {
        String normalized = accountId == null ? "" : accountId.trim();
        if (normalized.isEmpty()) {
            throw new BindingFactsParamException("账号标识不能为空");
        }
        if (normalized.length() > ACCOUNT_ID_MAX_LENGTH) {
            throw new BindingFactsParamException("账号标识长度不能超过 " + ACCOUNT_ID_MAX_LENGTH);
        }
        return normalized;
    }

    /**
     * VIN 规范化：去首尾空白并转大写后按统一规则校验（17 位、不含 I/O/Q）
     */
    private String normalizeVin(String vin) {
        String normalized = vin == null ? "" : vin.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new BindingFactsParamException("VIN 不能为空");
        }
        if (!VIN_PATTERN.matcher(normalized).matches()) {
            throw new BindingFactsParamException("VIN 格式不合法，应为 17 位且不含 I/O/Q");
        }
        return normalized;
    }

    /**
     * 存储侧 VIN 规范化：只做 trim/大写，不校验（历史数据不得因格式问题被静默改写或抛出）
     */
    private String normalizeStoredVin(String vin) {
        return vin == null ? "" : vin.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 状态与交付判定结果
     */
    private record Classification(BindingLookupStatus lookupStatus, BindingReasonCode reasonCode,
                                  BindingDeliveryStatus deliveryStatus, BindingFactsDeliveryPo deliveryRecord) {

        static Classification of(BindingLookupStatus lookupStatus, BindingReasonCode reasonCode) {
            return new Classification(lookupStatus, reasonCode, null, null);
        }

        static Classification matched(BindingDeliveryStatus deliveryStatus, BindingFactsDeliveryPo record) {
            Objects.requireNonNull(deliveryStatus, "deliveryStatus");
            return new Classification(BindingLookupStatus.MATCHED, null, deliveryStatus, record);
        }
    }
}
