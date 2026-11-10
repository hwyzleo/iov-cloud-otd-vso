package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 购车协议后台维护命令
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavePurchaseAgreementCmd {

    /** 协议业务 ID（新建时为空） */
    private String agreementId;

    private String agreementCode;

    /** 销售车型编码，GLOBAL 表示全局协议 */
    private String saleModelCode;

    private Integer versionNo;

    private String title;

    private String intro;

    private String detail;

    /** draft / active / off_shelf */
    private String agreementStatus;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    /** 操作人 */
    private Long operatorId;
}
