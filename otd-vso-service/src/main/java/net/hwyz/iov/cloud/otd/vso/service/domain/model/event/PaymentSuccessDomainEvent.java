package net.hwyz.iov.cloud.otd.vso.service.domain.model.event;

import lombok.Builder;
import lombok.Getter;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderDomainEvent;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentSuccessDomainEvent implements OrderDomainEvent {
    private String orderId;
    private String paymentId;
    private PaymentStage paymentStage;
    private BigDecimal paymentAmount;
    private LocalDateTime payTime;
    private LocalDateTime occurTime;

    @Override
    public String getEventType() { return "PAYMENT_SUCCESS"; }

    @Override
    public String getDescription() { return "支付成功，支付阶段：" + paymentStage + "，金额：" + paymentAmount; }

    @Override
    public LocalDateTime getOccurTime() { return this.occurTime != null ? this.occurTime : LocalDateTime.now(); }
}