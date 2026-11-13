package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付成功发布领域事件测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 支付成功必须发布 PaymentSuccessDomainEvent，供监听器在事务提交后取消超时任务；
 * 其他状态不得发布。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("支付回调 - 支付成功发布领域事件")
class PaymentCallbackCancelTimeoutTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CallbackLogRepository callbackLogRepository;
    @Mock
    private SupplementaryPaymentRepository supplementaryPaymentRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private CallbackSignatureService callbackSignatureService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .paymentId("PID1")
                .paymentNo("P1")
                .orderId("OID1")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentChannel("WECHAT")
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .build();

        OrderAmount amount = new OrderAmount("AMT1");
        amount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        amount.setPaidTotal(new Money(BigDecimal.ZERO));

        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .orderAmount(amount)
                .build();

        when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(true);
        when(paymentRepository.findByPaymentNo("P1")).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.of(amount));
    }

    private PaymentCallbackCmd cmd(PaymentStatus status) {
        return PaymentCallbackCmd.builder()
                .paymentNo("P1")
                .externalTradeNo("MOCK-P1")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(status.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + status)
                .build();
    }

    @Test
    @DisplayName("PAID 回调发布 PaymentSuccessDomainEvent")
    void 支付成功发布事件() {
        paymentCallbackService.handleCallback(cmd(PaymentStatus.PAID), "SIG", 1700000000L, "NONCE");

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(captor.capture());
        PaymentSuccessDomainEvent event = (PaymentSuccessDomainEvent) captor.getValue();
        assertEquals("OID1", event.getOrderId());
        assertEquals("P1", event.getPaymentId());
        assertEquals(PaymentStage.EARNEST_MONEY, event.getPaymentStage());
        assertEquals(0, new BigDecimal("5000.00").compareTo(event.getPaymentAmount()));
    }

    @Test
    @DisplayName("PROCESSING/PAYMENT_FAILED 回调不发布支付成功事件")
    void 非成功不发布事件() {
        when(paymentRepository.findByPaymentNo("P1")).thenReturn(Optional.of(Payment.builder()
                .paymentId("PID1").paymentNo("P1").orderId("OID1")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(PaymentStatus.PROCESSING)
                .build()));

        paymentCallbackService.handleCallback(cmd(PaymentStatus.PAYMENT_FAILED), "SIG", 1700000000L, "NONCE");

        verify(eventPublisher, never()).publishEvent(any(PaymentSuccessDomainEvent.class));
        verify(eventPublisher, never()).publishEvent(anyString());
    }
}
