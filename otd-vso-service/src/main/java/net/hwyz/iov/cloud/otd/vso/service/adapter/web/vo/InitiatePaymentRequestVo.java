package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;

/**
 * 发起支付请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InitiatePaymentRequestVo {

    @NotBlank(message = "小订单号不能为空")
    private String smallOrderNo;

    @NotNull(message = "支付渠道不能为空")
    private PaymentChannel paymentChannel;

}