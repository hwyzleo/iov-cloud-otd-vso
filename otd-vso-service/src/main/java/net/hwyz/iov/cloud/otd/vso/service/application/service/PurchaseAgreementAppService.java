package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AgreementConfirmationCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.SavePurchaseAgreementCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementConfirmationRequiredException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.AgreementVersionConflictException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAgreementConfirmation;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreement;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.PurchaseAgreementStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAgreementConfirmationRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PurchaseAgreementRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 购车协议应用服务
 * <p>
 * US-001：按车型及版本管理协议模板，订单保存确认快照；同时提供后台维护能力。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseAgreementAppService {

    private final PurchaseAgreementRepository purchaseAgreementRepository;
    private final OrderAgreementConfirmationRepository confirmationRepository;

    /**
     * C 端查询车型当前生效协议（车型专属优先，同代码取最高版本）
     */
    public List<PurchaseAgreementResult> listActiveAgreements(String saleModelCode) {
        return dedupeByCode(purchaseAgreementRepository.findActiveBySaleModel(saleModelCode, LocalDateTime.now()))
                .stream()
                .map(this::toResult)
                .collect(Collectors.toList());
    }

    /**
     * 校验下单协议确认并生成确认快照
     *
     * @param orderId       订单业务 ID
     * @param saleModelCode 销售车型编码
     * @param confirmations 请求提交的确认列表
     * @return 待保存的确认快照
     */
    public List<OrderAgreementConfirmation> buildConfirmations(String orderId, String saleModelCode,
                                                               List<AgreementConfirmationCmd> confirmations) {
        List<PurchaseAgreement> activeAgreements = dedupeByCode(
                purchaseAgreementRepository.findActiveBySaleModel(saleModelCode, LocalDateTime.now()));
        if (activeAgreements.isEmpty()) {
            log.warn("销售车型[{}]无生效购车协议，跳过协议确认校验", saleModelCode);
            return List.of();
        }
        if (confirmations == null || confirmations.isEmpty()) {
            throw new AgreementConfirmationRequiredException(
                    "下单必须确认当前生效购车协议：" + activeAgreements.stream()
                            .map(PurchaseAgreement::getAgreementCode).collect(Collectors.joining(",")));
        }

        Set<String> seenCodes = new HashSet<>();
        Map<String, PurchaseAgreement> confirmed = new LinkedHashMap<>();
        for (AgreementConfirmationCmd confirmation : confirmations) {
            if (confirmation.getAgreementCode() == null || confirmation.getAgreementCode().isEmpty()) {
                throw new AgreementConfirmationRequiredException("协议代码不能为空");
            }
            if (confirmation.getVersionNo() == null || confirmation.getVersionNo() < 1) {
                throw new AgreementConfirmationRequiredException("协议版本号必须大于等于 1");
            }
            if (!seenCodes.add(confirmation.getAgreementCode())) {
                throw new AgreementConfirmationRequiredException(
                        "协议[" + confirmation.getAgreementCode() + "]重复提交确认");
            }
            PurchaseAgreement agreement = activeAgreements.stream()
                    .filter(active -> active.getAgreementCode().equals(confirmation.getAgreementCode()))
                    .filter(active -> active.getVersionNo().equals(confirmation.getVersionNo()))
                    .findFirst()
                    .orElseThrow(() -> new AgreementNotExistException(
                            "协议[" + confirmation.getAgreementCode() + "]版本[" + confirmation.getVersionNo() + "]不存在或已失效"));
            confirmed.put(agreement.getAgreementCode(), agreement);
        }

        List<OrderAgreementConfirmation> snapshots = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (PurchaseAgreement agreement : activeAgreements) {
            if (!confirmed.containsKey(agreement.getAgreementCode())) {
                throw new AgreementConfirmationRequiredException(
                        "缺少必确认协议：" + agreement.getAgreementCode() + " v" + agreement.getVersionNo());
            }
        }
        for (PurchaseAgreement agreement : confirmed.values()) {
            snapshots.add(OrderAgreementConfirmation.snapshotOf(
                    IdUtil.fastSimpleUUID(), orderId, agreement, now));
        }
        return snapshots;
    }

    /**
     * 保存确认快照
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveConfirmations(List<OrderAgreementConfirmation> confirmations) {
        if (confirmations == null || confirmations.isEmpty()) {
            return;
        }
        confirmationRepository.saveAll(confirmations);
    }

    /**
     * 查询订单确认的协议快照
     */
    public List<PurchaseAgreementResult> listConfirmations(String orderId) {
        return confirmationRepository.findByOrderId(orderId).stream()
                .map(confirmation -> PurchaseAgreementResult.builder()
                        .agreementId(confirmation.getAgreementId())
                        .agreementCode(confirmation.getAgreementCode())
                        .versionNo(confirmation.getVersionNo())
                        .title(confirmation.getTitleSnapshot())
                        .intro(confirmation.getIntroSnapshot())
                        .detail(confirmation.getDetailSnapshot())
                        .build())
                .collect(Collectors.toList());
    }

    // ==================== 后台管理（MPT） ====================

    /**
     * 后台查询协议模板
     */
    public List<PurchaseAgreementResult> search(String saleModelCode, String agreementCode, String agreementStatus) {
        // PageUtil.convert 会保留 PageHelper 的 Page 元信息（total），供后台分页列表使用
        return net.hwyz.iov.cloud.framework.web.util.PageUtil.convert(
                purchaseAgreementRepository.search(saleModelCode, agreementCode, agreementStatus),
                this::toResult);
    }

    /**
     * 后台新增或修改协议模板
     */
    @Transactional(rollbackFor = Exception.class)
    public String save(SavePurchaseAgreementCmd cmd) {
        if (cmd.getAgreementCode() == null || cmd.getAgreementCode().isEmpty()) {
            throw new AgreementNotExistException("协议代码不能为空");
        }
        if (cmd.getVersionNo() == null || cmd.getVersionNo() < 1) {
            throw new AgreementNotExistException("协议版本号必须大于等于 1");
        }
        if (cmd.getTitle() == null || cmd.getTitle().isEmpty()) {
            throw new AgreementNotExistException("协议标题不能为空");
        }
        if (cmd.getDetail() == null || cmd.getDetail().isEmpty()) {
            throw new AgreementNotExistException("协议详情不能为空");
        }
        PurchaseAgreementStatus status = PurchaseAgreementStatus.fromValue(
                cmd.getAgreementStatus() == null ? PurchaseAgreementStatus.DRAFT.getValue() : cmd.getAgreementStatus());
        if (status == null) {
            throw new AgreementNotExistException("协议状态不合法：" + cmd.getAgreementStatus());
        }
        String agreementId = cmd.getAgreementId() == null || cmd.getAgreementId().isEmpty()
                ? "AGR" + IdUtil.nanoId(12)
                : cmd.getAgreementId();
        PurchaseAgreement agreement = PurchaseAgreement.builder()
                .agreementId(agreementId)
                .agreementCode(cmd.getAgreementCode())
                .saleModelCode(cmd.getSaleModelCode() == null || cmd.getSaleModelCode().isEmpty()
                        ? PurchaseAgreement.GLOBAL_SALE_MODEL_CODE : cmd.getSaleModelCode())
                .versionNo(cmd.getVersionNo())
                .title(cmd.getTitle())
                .intro(cmd.getIntro())
                .detail(cmd.getDetail())
                .agreementStatus(status)
                .effectiveFrom(cmd.getEffectiveFrom())
                .effectiveTo(cmd.getEffectiveTo())
                .build();
        // 同一车型 + 协议代码 + 版本号唯一：先做业务预校验，给出可读提示
        String saleModelCode = agreement.getSaleModelCode();
        boolean versionExists = purchaseAgreementRepository
                .search(saleModelCode, cmd.getAgreementCode(), null).stream()
                .anyMatch(existing -> cmd.getVersionNo().equals(existing.getVersionNo())
                        && !agreementId.equals(existing.getAgreementId()));
        if (versionExists) {
            throw new AgreementVersionConflictException(String.format(
                    "该车型[%s]下协议[%s]版本[%s]已存在", saleModelCode, cmd.getAgreementCode(), cmd.getVersionNo()));
        }
        try {
            purchaseAgreementRepository.save(agreement);
        } catch (DuplicateKeyException e) {
            // 并发下唯一键兜底：转成业务错误，避免前端只看到“系统接口异常”
            throw new AgreementVersionConflictException(String.format(
                    "该车型[%s]下协议[%s]版本[%s]已存在", saleModelCode, cmd.getAgreementCode(), cmd.getVersionNo()));
        }
        log.info("保存购车协议：agreementId={}, code={}, saleModel={}, version={}, status={}",
                agreementId, cmd.getAgreementCode(), agreement.getSaleModelCode(), cmd.getVersionNo(), status);
        return agreementId;
    }

    /**
     * 后台下架协议模板（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(String agreementId) {
        PurchaseAgreement agreement = purchaseAgreementRepository.findByAgreementId(agreementId)
                .orElseThrow(() -> new AgreementNotExistException("协议[" + agreementId + "]不存在"));
        PurchaseAgreement offShelfAgreement = PurchaseAgreement.builder()
                .agreementId(agreement.getAgreementId())
                .agreementCode(agreement.getAgreementCode())
                .saleModelCode(agreement.getSaleModelCode())
                .versionNo(agreement.getVersionNo())
                .title(agreement.getTitle())
                .intro(agreement.getIntro())
                .detail(agreement.getDetail())
                .agreementStatus(PurchaseAgreementStatus.OFF_SHELF)
                .effectiveFrom(agreement.getEffectiveFrom())
                .effectiveTo(agreement.getEffectiveTo())
                .build();
        purchaseAgreementRepository.save(offShelfAgreement);
    }

    private List<PurchaseAgreement> dedupeByCode(List<PurchaseAgreement> agreements) {
        Map<String, PurchaseAgreement> result = new LinkedHashMap<>();
        for (PurchaseAgreement agreement : agreements) {
            PurchaseAgreement existing = result.get(agreement.getAgreementCode());
            if (existing == null || shouldPrefer(agreement, existing)) {
                result.put(agreement.getAgreementCode(), agreement);
            }
        }
        return new ArrayList<>(result.values());
    }

    private boolean shouldPrefer(PurchaseAgreement candidate, PurchaseAgreement current) {
        if (candidate.isGlobal() != current.isGlobal()) {
            return !candidate.isGlobal();
        }
        return candidate.getVersionNo() > current.getVersionNo();
    }

    private PurchaseAgreementResult toResult(PurchaseAgreement agreement) {
        return PurchaseAgreementResult.builder()
                .agreementId(agreement.getAgreementId())
                .agreementCode(agreement.getAgreementCode())
                .saleModelCode(agreement.getSaleModelCode())
                .versionNo(agreement.getVersionNo())
                .title(agreement.getTitle())
                .intro(agreement.getIntro())
                .detail(agreement.getDetail())
                .agreementStatus(agreement.getAgreementStatus() == null ? null : agreement.getAgreementStatus().getValue())
                .effectiveFrom(agreement.getEffectiveFrom())
                .effectiveTo(agreement.getEffectiveTo())
                .build();
    }
}
