package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderPartyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventListener {

    private final OrderRepository orderRepository;
    private final TimeoutNotifyService timeoutNotifyService;
    private final WishlistRepository wishlistRepository;
    private final OrderPartyRepository orderPartyRepository;

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

        Optional<OrderPartyPo> orderPartyOpt = orderPartyRepository.findByOrderIdAndRole(order.getId(), "order_user");
        if (orderPartyOpt.isPresent()) {
            OrderPartyPo orderParty = orderPartyOpt.get();
            if (orderParty.getUserId() != null && !orderParty.getUserId().isEmpty()) {
                wishlistRepository.deleteByUserId(orderParty.getUserId());
                log.info("支付意向金成功后删除心愿单：accountId={}, orderNo={}",
                        orderParty.getUserId(), order.getOrderNo());
            }
        } else {
            log.warn("未找到订单客户信息，无法删除心愿单：orderId={}", order.getId());
        }
    }

}