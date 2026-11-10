package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单查询结果（设计 §11.4）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {

    private String paymentNo;
    private String orderNo;
    private String paymentStage;
    private String paymentChannel;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private String externalTradeNo;
    private LocalDateTime payTime;
    private LocalDateTime expireTime;
    private String failReason;
}
