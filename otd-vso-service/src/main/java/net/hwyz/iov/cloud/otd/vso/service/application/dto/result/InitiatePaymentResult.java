package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /** 支付状态（OTD-VSO-DSN-CR-002 §11.1） */
    private String paymentStatus;

    /** 外部交易单号 */
    private String externalTradeNo;

    /** 支付截止时间 */
    private LocalDateTime expireTime;

}