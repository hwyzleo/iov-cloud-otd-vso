package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;

import java.time.LocalDateTime;

/**
 * 购车合同聚合（OTD-VSO-DSN-CR-014）
 * <p>
 * 合同事实源；状态只能由本聚合方法推进，SIGNED/EFFECTIVE 仅允许签署回调驱动：
 * <ul>
 *     <li>DRAFT → GENERATED：保存电子签生成的文件事实；</li>
 *     <li>GENERATED / INVALID → SIGNING：保存当前签署任务号；</li>
 *     <li>SIGNING → SIGNED / EFFECTIVE / INVALID：仅回调路径；</li>
 *     <li>SIGNED → EFFECTIVE：合法生效回调；</li>
 *     <li>SIGNED / EFFECTIVE 为签署终态，冲突回调一律拒绝。</li>
 * </ul>
 *
 * @author VSO Team
 */
@Getter
@Setter
@NoArgsConstructor
public class Contract {

    /**
     * 购车合同类型
     */
    public static final String TYPE_VEHICLE_PURCHASE = "VEHICLE_PURCHASE";

    /**
     * 本期签署方式：Mock 电子签
     */
    public static final String SIGN_MODE_MOCK = "MOCK";

    /**
     * 生效规则：签署后生效
     */
    public static final String SIGN_EFFECTIVE_RULE_SIGNED = "SIGNED";

    private String contractId;

    private String orderId;

    private String contractNo;

    private String contractType;

    private Integer versionNo;

    private ContractStatus contractStatus;

    private String signMode;

    private String signEffectiveRule;

    private String fileUrl;

    private String externalContractNo;

    /**
     * 当前签署任务号（与 externalContractNo 不得混用）
     */
    private String signNo;

    private LocalDateTime generateTime;

    private LocalDateTime signTime;

    private LocalDateTime effectiveTime;

    private LocalDateTime invalidTime;

    private String invalidReason;

    /**
     * 新建草稿合同
     */
    public static Contract draft(String contractId, String orderId, String contractNo) {
        Contract contract = new Contract();
        contract.contractId = contractId;
        contract.orderId = orderId;
        contract.contractNo = contractNo;
        contract.contractType = TYPE_VEHICLE_PURCHASE;
        contract.versionNo = 1;
        contract.contractStatus = ContractStatus.DRAFT;
        contract.signMode = SIGN_MODE_MOCK;
        contract.signEffectiveRule = SIGN_EFFECTIVE_RULE_SIGNED;
        return contract;
    }

    /**
     * 保存电子签生成结果（DRAFT → GENERATED）
     */
    public void markGenerated(String externalContractNo, String fileUrl, LocalDateTime generateTime) {
        requireStatus(ContractStatus.DRAFT, "生成合同");
        this.externalContractNo = externalContractNo;
        this.fileUrl = fileUrl;
        this.generateTime = generateTime == null ? LocalDateTime.now() : generateTime;
        this.contractStatus = ContractStatus.GENERATED;
    }

    /**
     * 保存签署任务（GENERATED / INVALID → SIGNING）
     */
    public void markSigning(String signNo) {
        if (signNo == null || signNo.isBlank()) {
            throw new IllegalStateException("签署任务号不能为空");
        }
        if (this.contractStatus != ContractStatus.GENERATED && this.contractStatus != ContractStatus.INVALID) {
            throw new IllegalStateException("合同[" + contractNo + "]当前状态[" + contractStatus + "]不允许发起签署");
        }
        this.signNo = signNo;
        this.contractStatus = ContractStatus.SIGNING;
        this.invalidTime = null;
        this.invalidReason = null;
    }

    /**
     * 签署成功（SIGNING → SIGNED，仅回调路径）
     */
    public void markSigned(LocalDateTime signTime) {
        requireStatus(ContractStatus.SIGNING, "签署合同");
        this.signTime = signTime == null ? LocalDateTime.now() : signTime;
        this.contractStatus = ContractStatus.SIGNED;
    }

    /**
     * 合同生效（SIGNING / SIGNED → EFFECTIVE，仅回调路径）
     */
    public void markEffective(LocalDateTime effectiveTime) {
        if (this.contractStatus == ContractStatus.EFFECTIVE) {
            return;
        }
        requireStatus(ContractStatus.SIGNED, "合同生效");
        this.effectiveTime = effectiveTime == null ? LocalDateTime.now() : effectiveTime;
        this.contractStatus = ContractStatus.EFFECTIVE;
    }

    /**
     * 合同失效（SIGNING → INVALID，仅回调路径；终态不可回退）
     */
    public void markInvalid(LocalDateTime invalidTime, String reason) {
        requireStatus(ContractStatus.SIGNING, "合同失效");
        this.invalidTime = invalidTime == null ? LocalDateTime.now() : invalidTime;
        this.invalidReason = reason;
        this.contractStatus = ContractStatus.INVALID;
    }

    /**
     * 是否处于签署终态（SIGNED / EFFECTIVE）：终态重复发起签署必须幂等返回，不得新建合同或任务
     */
    public boolean isSignTerminal() {
        return this.contractStatus == ContractStatus.SIGNED || this.contractStatus == ContractStatus.EFFECTIVE;
    }

    /**
     * 是否满足尾款准入门槛（SIGNED / EFFECTIVE）
     */
    public boolean isSignEffectiveForTailPayment() {
        return isSignTerminal();
    }

    private void requireStatus(ContractStatus expected, String action) {
        if (this.contractStatus != expected) {
            throw new IllegalStateException(
                    "合同[" + contractNo + "]当前状态[" + contractStatus + "]不允许" + action);
        }
    }
}
