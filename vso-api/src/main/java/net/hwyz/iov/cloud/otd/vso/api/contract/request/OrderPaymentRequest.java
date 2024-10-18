package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.PaymentChannel;

import java.math.BigDecimal;

/**
 * 订单支付请求
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentRequest {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNum;
    /**
     * 订单支付阶段
     */
    @NotNull(message = "订单支付阶段不能为空")
    private Integer orderPaymentPhase;
    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    private BigDecimal paymentAmount;
    /**
     * 支付渠道
     */
    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;

}
