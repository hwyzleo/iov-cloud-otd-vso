package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付回调测试（OTD-VSO-DSN-CR-002 §14 必测）
 * <p>
 * 覆盖四状态、签名、金额、阶段、重复与冲突。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("支付回调 - 四状态/签名/幂等/冲突")
class PaymentCallbackServiceTest {

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

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    private Payment payment;
    private Order order;
    private OrderAmount orderAmount;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .paymentId("PID1")
                .paymentNo("P001")
                .orderId("OID1")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentChannel("WECHAT")
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .build();

        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .orderType(net.hwyz.iov.cloud.otd.vso.api.enums.OrderType.SMALL)
                .build();

        orderAmount = new OrderAmount("AMT1");
        orderAmount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        orderAmount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        orderAmount.setPaidTotal(new Money(BigDecimal.ZERO));
        orderAmount.setRefundTotal(new Money(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("签名/防重放失败：拒绝且不改变支付与订单")
    void 签名失败_拒绝() {
        when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(false);

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("支付单不存在：拒绝")
    void 支付单不存在_拒绝() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.empty());

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
    }

    @Test
    @DisplayName("金额不一致：拒绝且不累计")
    void 金额不一致_拒绝() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(payment));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "500.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("支付阶段不匹配：拒绝")
    void 阶段不匹配_拒绝() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(payment));

        PaymentCallbackCmd cmd = cmd(PaymentStatus.PAID, "5000.00");
        cmd.setPaymentStage(PaymentStage.DOWN_PAYMENT.name());
        PaymentCallbackResult result = paymentCallbackService.handleCallback(cmd, "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
    }

    @Test
    @DisplayName("PAID：同一事务更新支付、订单金额、订单状态与时间线")
    void 支付成功_累计并推进订单() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(copyOf(payment)));
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.of(orderAmount));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals(OrderState.EARNEST_MONEY_PAID, order.getOrderState());
        assertEquals(0, orderAmount.getPaidTotal().getAmount().compareTo(new BigDecimal("5000.00")));
        verify(paymentRepository).save(any(Payment.class));
        verify(orderRepository).save(order);
        verify(orderTimelineAppService).record(eq("OID1"), eq(OrderTimelineEventType.PAYMENT_SUCCESS),
                any(), any(), anyString(), anyString(), anyString(), eq("P001"), any(), any());
    }

    @Test
    @DisplayName("重复 PAID：幂等返回且不重复累计金额")
    void 重复支付成功_不重复累计() {
        payment.markPaid("MOCK-P001", LocalDateTime.now());
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(payment));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.DUPLICATE, result.getCode());
        verify(orderRepository, never()).save(any());
        verify(orderAmountRepository, never()).findDomainByOrderId(anyString());
    }

    @Test
    @DisplayName("已 PAID 后收到 PAYMENT_FAILED：返回冲突并保留 PAID")
    void 已成功后失败_冲突() {
        payment.markPaid("MOCK-P001", LocalDateTime.now());
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(payment));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAYMENT_FAILED, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.CONFLICT, result.getCode());
        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("PROCESSING：支付单处理中，订单保持待支付")
    void 处理中_订单不变() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(copyOf(payment)));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PROCESSING, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        verify(orderRepository, never()).save(any());
        verify(orderTimelineAppService).record(eq("OID1"), eq(OrderTimelineEventType.PAYMENT_PROCESSING),
                any(), any(), anyString(), anyString(), anyString(), eq("P001"), any(), any());
    }

    @Test
    @DisplayName("PAYMENT_FAILED：保存失败，订单保持待支付并允许重新支付")
    void 支付失败_订单保持待支付() {
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(copyOf(payment)));

        PaymentCallbackCmd cmd = cmd(PaymentStatus.PAYMENT_FAILED, "5000.00");
        cmd.setFailReason("余额不足");
        PaymentCallbackResult result = paymentCallbackService.handleCallback(cmd, "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("CANCELLED 后收到 PAID：冲突且不恢复状态")
    void 已取消后成功_冲突() {
        payment.markCancelled("用户取消");
        stubSignatureOk();
        when(paymentRepository.findByPaymentNo("P001")).thenReturn(Optional.of(payment));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(
                cmd(PaymentStatus.PAID, "5000.00"), "sig", 1L, "nonce");

        assertEquals(PaymentCallbackResultCode.CONFLICT, result.getCode());
        verify(orderRepository, never()).save(any());
        assertTrue(payment.getPaymentStatus() == PaymentStatus.CANCELLED);
    }

    private void stubSignatureOk() {
        when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(true);
    }

    private PaymentCallbackCmd cmd(PaymentStatus status, String amount) {
        return PaymentCallbackCmd.builder()
                .paymentNo("P001")
                .externalTradeNo("MOCK-P001")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal(amount))
                .paymentStatus(status.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + status)
                .build();
    }

    private Payment copyOf(Payment source) {
        return Payment.builder()
                .paymentId(source.getPaymentId())
                .paymentNo(source.getPaymentNo())
                .orderId(source.getOrderId())
                .paymentStage(source.getPaymentStage())
                .paymentChannel(source.getPaymentChannel())
                .paymentAmount(source.getPaymentAmount())
                .paymentStatus(source.getPaymentStatus())
                .build();
    }
}
