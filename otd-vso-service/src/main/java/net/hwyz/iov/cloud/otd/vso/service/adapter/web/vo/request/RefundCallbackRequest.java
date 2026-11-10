package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款回调请求（Mock 退款网关）
 *
 * @author VSO Team
 */
@Data
public class RefundCallbackRequest {

    @NotBlank(message = "退款单号不能为空")
    private String refundNo;

    @NotBlank(message = "退款明细ID不能为空")
    private String refundItemId;

    @NotBlank(message = "外部退款单号不能为空")
    private String externalRefundNo;

    @NotNull(message = "退款金额不能为空")
    private BigDecimal refundAmount;

    @NotBlank(message = "退款状态不能为空")
    private String refundStatus;

    private LocalDateTime refundTime;

    private String idempotentKey;

    private String failReason;

    private Long timestamp;

    private String nonce;
}
