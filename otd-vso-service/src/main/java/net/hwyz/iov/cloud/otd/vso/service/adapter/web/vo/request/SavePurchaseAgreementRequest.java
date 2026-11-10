package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购车协议维护请求（MPT 后台）
 *
 * @author VSO Team
 */
@Data
public class SavePurchaseAgreementRequest {

    private String agreementId;

    @NotBlank(message = "协议代码不能为空")
    private String agreementCode;

    /** GLOBAL 表示全局协议 */
    private String saleModelCode;

    @NotNull(message = "协议版本号不能为空")
    @Min(value = 1, message = "协议版本号必须大于等于 1")
    private Integer versionNo;

    @NotBlank(message = "协议标题不能为空")
    private String title;

    private String intro;

    @NotBlank(message = "协议详情不能为空")
    private String detail;

    /** draft / active / off_shelf */
    private String agreementStatus;

    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;
}
