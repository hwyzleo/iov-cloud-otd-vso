package net.hwyz.iov.cloud.otd.vso.api.vo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发起补款支付请求VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateSupplementPaymentRequestVo {

    /**
     * 补款单号
     */
    @NotBlank(message = "补款单号不能为空")
    private String supplementaryNo;

    /**
     * 支付渠道
     */
    @NotBlank(message = "支付渠道不能为空")
    private String paymentChannel;
}
