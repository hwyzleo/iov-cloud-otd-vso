package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付回调请求
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCallbackRequest {

    @NotBlank(message = "支付单号不能为空")
    private String paymentNo;

    @NotBlank(message = "外部交易单号不能为空")
    private String externalTradeNo;

    @NotBlank(message = "支付阶段不能为空")
    private String paymentStage;

    @NotNull(message = "支付金额不能为空")
    @Positive(message = "支付金额必须大于0")
    private BigDecimal paymentAmount;

    @NotBlank(message = "支付状态不能为空")
    private String paymentStatus;

    @NotNull(message = "支付时间不能为空")
    private LocalDateTime payTime;

    private String idempotentKey;

    private String signature;

}