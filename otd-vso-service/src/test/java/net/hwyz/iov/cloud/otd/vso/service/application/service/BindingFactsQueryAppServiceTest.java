package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.BindingFactsQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.BindingFactsResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BindingFactsParamException;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.BindingFactsRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsDeliveryPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsOrderPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.BindingFactsPartyPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 绑车事实查询分类单测（OTD-VSO-DSN-CR-017，覆盖 AT-01～08、AT-11）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("绑车事实查询 - 分类单测")
class BindingFactsQueryAppServiceTest {

    private static final String ACCOUNT = "ACC-1001";
    private static final String VIN = "LSVAA1234K2100001";
    private static final String ORDER_ID = "ORD-1";
    private static final String ORDER_NO = "SO202611200001";
    private static final LocalDateTime DELIVERED_AT = LocalDateTime.of(2026, 11, 20, 6, 30, 15);

    @Mock
    private BindingFactsRepository bindingFactsRepository;

    private BindingFactsQueryAppService service;

    @BeforeEach
    void setUp() {
        service = new BindingFactsQueryAppService(bindingFactsRepository, BusinessTimeZone.utc());
    }

    @Test
    @DisplayName("无同时匹配账号与 VIN 的有效订单返回 NO_MATCH，且不包含事实")
    void 无匹配返回NO_MATCH() {
        when(bindingFactsRepository.findCandidateOrders(anyString(), anyString(), anyList(), anyInt()))
                .thenReturn(List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.NO_MATCH);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.NO_ACCOUNT_VIN_MATCH);
        assertThat(result.getFacts()).isEmpty();
        assertThat(result.getSchemaVersion()).isEqualTo(BindingFactsQueryAppService.SCHEMA_VERSION);
        assertThat(result.getCheckedAt()).isNotNull();
        verify(bindingFactsRepository, never()).findMatchedParties(anyString(), anyList(), anyList());
    }

    @Test
    @DisplayName("两个以上订单候选返回 AMBIGUOUS，不自动选择最新或已交付单")
    void 多候选返回AMBIGUOUS() {
        when(bindingFactsRepository.findCandidateOrders(anyString(), anyString(), anyList(), anyInt()))
                .thenReturn(List.of(order(ORDER_ID, 650), order("ORD-2", 620)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.AMBIGUOUS);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.MULTIPLE_ORDER_MATCHES);
        assertThat(result.getFacts()).isEmpty();
    }

    @Test
    @DisplayName("唯一交付前订单无交付记录返回 MATCHED/NOT_DELIVERED，角色合并去重")
    void 交付前状态返回未交付() {
        mockCandidate(order(ORDER_ID, 620));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(
                        party("P-1", "order_user"),
                        party("P-2", "buyer"),
                        party("P-3", "buyer")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.MATCHED);
        assertThat(result.getReasonCode()).isNull();
        assertThat(result.getFacts()).hasSize(1);
        BindingFactsResult.Fact fact = result.getFacts().get(0);
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.NOT_DELIVERED);
        assertThat(fact.getDeliveryRecordId()).isNull();
        assertThat(fact.getActualDeliveryTime()).isNull();
        assertThat(fact.getVin()).isEqualTo(VIN);
        assertThat(fact.getMatchedRoles()).containsExactly("order_user", "buyer");
        assertThat(fact.getEvidenceRefs()).extracting(BindingFactsResult.EvidenceRef::getType)
                .containsExactly(BindingEvidenceType.ORDER, BindingEvidenceType.PARTY,
                        BindingEvidenceType.PARTY, BindingEvidenceType.PARTY);
    }

    @Test
    @DisplayName("650 且交付证据完整返回 DELIVERED，返回原交付记录引用与时间")
    void 已交付返回DELIVERED() {
        mockDeliveredOrder(650, List.of(delivery("DR-1", "delivered", DELIVERED_AT, VIN, 7)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.MATCHED);
        BindingFactsResult.Fact fact = result.getFacts().get(0);
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.DELIVERED);
        assertThat(fact.getDeliveryRecordId()).isEqualTo("DR-1");
        // 交付记录持久化为业务时区墙钟，对外必须换算成绝对时刻（UTC 时区下同一时刻）
        assertThat(fact.getActualDeliveryTime()).isEqualTo(Instant.parse("2026-11-20T06:30:15Z"));
        assertThat(fact.getEvidenceRefs()).extracting(BindingFactsResult.EvidenceRef::getType)
                .contains(BindingEvidenceType.DELIVERY);
        assertThat(fact.getEvidenceRefs()).filteredOn(ref -> ref.getType() == BindingEvidenceType.DELIVERY)
                .singleElement()
                .satisfies(ref -> {
                    assertThat(ref.getId()).isEqualTo("DR-1");
                    assertThat(ref.getVersion()).isEqualTo(7);
                });
    }

    @Test
    @DisplayName("900 与 650 使用同一已交付口径，交付事实引用不变")
    void 办结900保持交付口径() {
        mockDeliveredOrder(900, List.of(delivery("DR-1", "delivered", DELIVERED_AT, VIN, 7)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        BindingFactsResult.Fact fact = result.getFacts().get(0);
        assertThat(fact.getDeliveryStatus()).isEqualTo(BindingDeliveryStatus.DELIVERED);
        assertThat(fact.getDeliveryRecordId()).isEqualTo("DR-1");
        assertThat(fact.getActualDeliveryTime()).isEqualTo(Instant.parse("2026-11-20T06:30:15Z"));
    }

    @Test
    @DisplayName("业务时区非 UTC 时，墙钟交付时间换算为绝对时刻，不输出墙钟冒充 UTC")
    void 非UTC时区换算绝对时刻() {
        BindingFactsQueryAppService shanghai =
                new BindingFactsQueryAppService(bindingFactsRepository, BusinessTimeZone.of(ZoneId.of("Asia/Shanghai")));
        when(bindingFactsRepository.findCandidateOrders(anyString(), anyString(), anyList(), anyInt()))
                .thenReturn(List.of(order(ORDER_ID, 650)));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList()))
                .thenReturn(List.of(delivery("DR-1", "delivered",
                        LocalDateTime.of(2026, 11, 20, 14, 30, 15), VIN, 7)));

        BindingFactsResult result = shanghai.query(query(ACCOUNT, VIN));

        assertThat(result.getFacts().get(0).getActualDeliveryTime())
                .isEqualTo(Instant.parse("2026-11-20T06:30:15Z"));
        assertThat(result.getCheckedAt()).isNotNull();
    }

    @Test
    @DisplayName("650 缺少交付记录返回 INSUFFICIENT，不补造交付事实")
    void 缺交付记录返回INSUFFICIENT() {
        mockDeliveredOrder(650, List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INSUFFICIENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.MISSING_DELIVERY_EVIDENCE);
        assertThat(result.getFacts()).isEmpty();
    }

    @Test
    @DisplayName("650 交付记录缺实际交付时间返回 INSUFFICIENT")
    void 缺实际交付时间返回INSUFFICIENT() {
        mockDeliveredOrder(650, List.of(delivery("DR-1", "delivered", null, VIN, 7)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INSUFFICIENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.MISSING_DELIVERY_EVIDENCE);
    }

    @Test
    @DisplayName("650 交付记录 VIN 与订单不一致返回 INCONSISTENT")
    void 交付VIN不一致返回INCONSISTENT() {
        mockDeliveredOrder(650,
                List.of(delivery("DR-1", "delivered", DELIVERED_AT, "LSVAA1234K2100009", 7)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.VIN_EVIDENCE_MISMATCH);
    }

    @Test
    @DisplayName("650 存在两条互斥交付记录返回 INCONSISTENT，不挑任意一条")
    void 多条交付记录返回INCONSISTENT() {
        mockDeliveredOrder(650, List.of(
                delivery("DR-1", "delivered", DELIVERED_AT, VIN, 7),
                delivery("DR-2", "delivered", DELIVERED_AT.plusDays(1), VIN, 1)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.STATE_EVIDENCE_CONFLICT);
    }

    @Test
    @DisplayName("650 但订单无 VIN 关联返回 INCONSISTENT（身份关联不完整不得当已交付）")
    void 订单VIN缺失返回INCONSISTENT() {
        mockCandidate(order(ORDER_ID, 650, null));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList()))
                .thenReturn(List.of(delivery("DR-1", "delivered", DELIVERED_AT, VIN, 7)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.VIN_EVIDENCE_MISMATCH);
    }

    @Test
    @DisplayName("交付前状态却存在有效交付记录返回 INCONSISTENT")
    void 交付前状态有交付记录返回INCONSISTENT() {
        mockDeliveredOrder(620, List.of(delivery("DR-1", "delivered", DELIVERED_AT, VIN, 3)));

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.STATE_EVIDENCE_CONFLICT);
    }

    @Test
    @DisplayName("历史 700 状态返回 INSUFFICIENT/UNSUPPORTED_LEGACY_STATE，不按已交付推定")
    void 历史700返回INSUFFICIENT() {
        mockCandidate(order(ORDER_ID, 700));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INSUFFICIENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.UNSUPPORTED_LEGACY_STATE);
        assertThat(result.getFacts()).isEmpty();
    }

    @Test
    @DisplayName("取消/关闭等无法安全解释状态返回 INCONSISTENT")
    void 取消状态返回INCONSISTENT() {
        mockCandidate(order(ORDER_ID, 950));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.INCONSISTENT);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.STATE_EVIDENCE_CONFLICT);
    }

    @Test
    @DisplayName("小写与首尾空白的 VIN 规范化后按大写匹配")
    void VIN规范化() {
        mockCandidate(order(ORDER_ID, 620));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(List.of());

        BindingFactsResult result = service.query(query(" " + ACCOUNT + " ", " " + VIN.toLowerCase() + " "));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.MATCHED);
        assertThat(result.getFacts().get(0).getVin()).isEqualTo(VIN);
        verify(bindingFactsRepository).findCandidateOrders(ACCOUNT, VIN,
                BindingFactsQueryAppService.MATCHED_ROLES, BindingFactsQueryAppService.CANDIDATE_LIMIT);
    }

    @Test
    @DisplayName("非法 VIN（长度/含 I O Q/空）按参数错误拒绝，不返回 NO_MATCH")
    void 非法VIN返回参数错误() {
        for (String vin : List.of("", "   ", "LSVAA1234K21000", "LSVAA1234K21000O1", "LSVAA1234K2100I01")) {
            assertThatThrownBy(() -> service.query(query(ACCOUNT, vin)))
                    .as("VIN[" + vin + "] 必须按参数错误拒绝")
                    .isInstanceOf(BindingFactsParamException.class);
        }
        verify(bindingFactsRepository, never()).findCandidateOrders(anyString(), anyString(), anyList(), anyInt());
    }

    @Test
    @DisplayName("账号缺失或超长按参数错误拒绝")
    void 非法账号返回参数错误() {
        assertThatThrownBy(() -> service.query(query("  ", VIN)))
                .isInstanceOf(BindingFactsParamException.class);
        assertThatThrownBy(() -> service.query(query("A".repeat(65), VIN)))
                .isInstanceOf(BindingFactsParamException.class);
        assertThatThrownBy(() -> service.query(query(null, null)))
                .isInstanceOf(BindingFactsParamException.class);
    }

    @Test
    @DisplayName("候选订单角色行缺失时不虚构关联，按 NO_MATCH 返回")
    void 角色行缺失返回NO_MATCH() {
        mockCandidate(order(ORDER_ID, 620));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of());

        BindingFactsResult result = service.query(query(ACCOUNT, VIN));

        assertThat(result.getLookupStatus()).isEqualTo(BindingLookupStatus.NO_MATCH);
        assertThat(result.getReasonCode()).isEqualTo(BindingReasonCode.NO_ACCOUNT_VIN_MATCH);
        verify(bindingFactsRepository, never()).findDeliveryRecords(anyList());
    }

    @Test
    @DisplayName("查询只涉及只读仓储，不触达任何写仓储或事件端口")
    void 只读仓储契约() {
        mockCandidate(order(ORDER_ID, 620));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(List.of());

        service.query(query(ACCOUNT, VIN));

        assertThat(BindingFactsRepository.class.getDeclaredMethods())
                .allSatisfy(method -> assertThat(method.getName()).startsWith("find"));
        verify(bindingFactsRepository).findCandidateOrders(anyString(), anyString(), anyList(), anyInt());
        verify(bindingFactsRepository).findMatchedParties(anyString(), anyList(), anyList());
        verify(bindingFactsRepository).findDeliveryRecords(anyList());
    }

    @Test
    @DisplayName("仓储技术异常向上抛出，不得降级成 NO_MATCH 空成功")
    void 技术失败不降级() {
        when(bindingFactsRepository.findCandidateOrders(anyString(), anyString(), anyList(), anyInt()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("db timeout"));

        assertThatThrownBy(() -> service.query(query(ACCOUNT, VIN)))
                .isInstanceOf(org.springframework.dao.DataAccessResourceFailureException.class);
    }

    private void mockCandidate(BindingFactsOrderPo order) {
        when(bindingFactsRepository.findCandidateOrders(anyString(), anyString(), anyList(), anyInt()))
                .thenReturn(List.of(order));
    }

    private void mockDeliveredOrder(int orderState, List<BindingFactsDeliveryPo> records) {
        mockCandidate(order(ORDER_ID, orderState));
        when(bindingFactsRepository.findMatchedParties(anyString(), anyList(), anyList()))
                .thenReturn(List.of(party("P-1", "order_user")));
        when(bindingFactsRepository.findDeliveryRecords(anyList())).thenReturn(records);
    }

    private BindingFactsQuery query(String accountId, String vin) {
        return BindingFactsQuery.builder().accountId(accountId).vin(vin).build();
    }

    private BindingFactsOrderPo order(String orderId, int orderState) {
        return order(orderId, orderState, VIN);
    }

    private BindingFactsOrderPo order(String orderId, int orderState, String vehicleVin) {
        BindingFactsOrderPo po = new BindingFactsOrderPo();
        po.setOrderId(orderId);
        po.setOrderNo(ORDER_NO);
        po.setOrderState(orderState);
        po.setVehicleVin(vehicleVin);
        po.setRowVersion(3);
        return po;
    }

    private BindingFactsPartyPo party(String partyId, String role) {
        BindingFactsPartyPo po = new BindingFactsPartyPo();
        po.setPartyId(partyId);
        po.setOrderId(ORDER_ID);
        po.setPartyRole(role);
        po.setRowVersion(2);
        return po;
    }

    private BindingFactsDeliveryPo delivery(String recordId, String status, LocalDateTime actualDeliveryTime,
                                            String vin, int rowVersion) {
        BindingFactsDeliveryPo po = new BindingFactsDeliveryPo();
        po.setDeliveryRecordId(recordId);
        po.setOrderId(ORDER_ID);
        po.setDeliveryStatus(status);
        po.setActualDeliveryTime(actualDeliveryTime);
        po.setVehicleVin(vin);
        po.setRowVersion(rowVersion);
        return po;
    }
}
