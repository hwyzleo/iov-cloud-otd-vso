package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 购车协议模板领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseAgreement {

    /** 全局协议标识 */
    public static final String GLOBAL_SALE_MODEL_CODE = "GLOBAL";

    private String agreementId;

    private String agreementCode;

    private String saleModelCode;

    private Integer versionNo;

    private String title;

    private String intro;

    private String detail;

    private PurchaseAgreementStatus agreementStatus;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    /**
     * 当前是否生效：active、from<=now、to 为空或 >now
     */
    public boolean isValid(LocalDateTime now) {
        if (agreementStatus != PurchaseAgreementStatus.ACTIVE) {
            return false;
        }
        if (effectiveFrom != null && effectiveFrom.isAfter(now)) {
            return false;
        }
        return effectiveTo == null || effectiveTo.isAfter(now);
    }

    /**
     * 是否全局协议
     */
    public boolean isGlobal() {
        return GLOBAL_SALE_MODEL_CODE.equalsIgnoreCase(saleModelCode);
    }
}
