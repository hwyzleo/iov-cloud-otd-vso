package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 购车协议结果（C 端查询与后台管理共用）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseAgreementResult {

    private String agreementId;
    private String agreementCode;
    private String saleModelCode;
    private Integer versionNo;
    private String title;
    private String intro;
    private String detail;
    private String agreementStatus;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
