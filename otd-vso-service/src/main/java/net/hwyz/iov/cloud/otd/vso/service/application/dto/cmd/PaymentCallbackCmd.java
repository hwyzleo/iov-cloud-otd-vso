package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付回调命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class PaymentCallbackCmd {

    private String paymentNo;
    private String externalTradeNo;
    private String paymentStage;
    private BigDecimal paymentAmount;
    private String paymentStatus;
    private LocalDateTime payTime;
    private String idempotentKey;

}