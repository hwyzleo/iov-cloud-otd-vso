package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;

import java.math.BigDecimal;

/**
 * 发起支付结果
 *
 * @author hwyz_leo
 */
@Data
@Builder
public class InitiatePaymentResult {

    private String paymentNo;
    private PaymentChannel paymentChannel;
    private BigDecimal paymentAmount;
    private String paymentMerchant;
    private String paymentReference;

}