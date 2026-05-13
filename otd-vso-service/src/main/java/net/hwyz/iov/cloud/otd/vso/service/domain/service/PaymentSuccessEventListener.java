package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventListener {

    private final OrderRepository orderRepository;
    private final TimeoutNotifyService timeoutNotifyService;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void handlePaymentSuccess(PaymentSuccessDomainEvent event) {
        log.info("处理支付成功事件：orderId={}, paymentStage={}, amount={}",
                event.getOrderId(), event.getPaymentStage(), event.getPaymentAmount());

        Order order = orderRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("订单不存在：" + event.getOrderId()));

        if (event.getPaymentStage() == PaymentStage.EARNEST_MONEY) {
            handleEarnestMoneyPayment(order, event.getPaymentAmount());
        }

        timeoutNotifyService.cancelByOrderIdAndType(event.getOrderId(), "SMALL_ORDER_PAY_TIMEOUT");

        orderRepository.save(order);

        log.info("支付成功事件处理完成：orderId={}", event.getOrderId());
    }

    private void handleEarnestMoneyPayment(Order order, BigDecimal amount) {
        order.pay(amount);
        log.info("意向金支付成功，订单状态更新：orderId={}, newState={}",
                order.getId(), order.getOrderState());
    }

}