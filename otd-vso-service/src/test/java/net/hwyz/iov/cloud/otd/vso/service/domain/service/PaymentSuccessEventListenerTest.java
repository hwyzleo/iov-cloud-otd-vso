package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSuccessEventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TimeoutNotifyService timeoutNotifyService;

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private PaymentSuccessEventListener eventListener;

    private Order order;
    private PaymentSuccessDomainEvent event;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("order_001")
                .orderNo("VSO20240101001")
                .orderPersonId("user_001")
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .saleModel("MODEL001")
                .build();

        event = PaymentSuccessDomainEvent.builder()
                .orderId("order_001")
                .paymentId("payment_001")
                .paymentStage(PaymentStage.EARNEST_MONEY)
                .paymentAmount(new BigDecimal("1000.00"))
                .payTime(LocalDateTime.now())
                .occurTime(LocalDateTime.now())
                .build();
    }

    @Test
    void testHandlePaymentSuccess_ShouldUpdateOrderState() {
        when(orderRepository.findByOrderId("order_001")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        eventListener.handlePaymentSuccess(event);

        verify(orderRepository).findByOrderId("order_001");
        verify(orderRepository).save(any(Order.class));
        verify(timeoutNotifyService).cancelByOrderIdAndType("order_001", "SMALL_ORDER_PAY_TIMEOUT");
    }

    @Test
    void testHandlePaymentSuccess_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findByOrderId("order_001")).thenReturn(Optional.empty());

        try {
            eventListener.handlePaymentSuccess(event);
        } catch (RuntimeException e) {
            assertEquals("订单不存在：order_001", e.getMessage());
        }
    }
}