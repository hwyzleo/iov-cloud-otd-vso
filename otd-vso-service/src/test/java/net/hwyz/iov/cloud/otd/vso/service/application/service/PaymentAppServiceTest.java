package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 支付发起与查询测试（US-004 / §14：活动单返回、过期关闭、金额快照、Mock 不固定成功）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("支付发起 - 金额快照/活动支付单/Mock 边界")
class PaymentAppServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private PaymentAdapter paymentAdapter;
    @Mock
    private PaymentChannelConfig paymentChannelConfig;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private PaymentAppService paymentAppService;

    private Order order;
    private OrderAmount orderAmount;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(OrderState.DOWN_PAYMENT_UNPAID)
                .build();
        orderAmount = new OrderAmount("AMT1");
        orderAmount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        orderAmount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        orderAmount.setPaidTotal(new Money(BigDecimal.ZERO));

        lenient().when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));
        lenient().when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.of(orderAmount));
        lenient().when(paymentChannelConfig.isChannelEnabled(PaymentChannel.WECHAT)).thenReturn(true);
        lenient().when(paymentChannelConfig.getDownPaymentTimeoutMinutes()).thenReturn(60);
        lenient().when(paymentChannelConfig.getSmallOrderTimeoutMinutes()).thenReturn(30);
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
        lenient().when(transactionManager.getTransaction(any()))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("支付金额取订单金额快照（定金 20000，不取车型最低价）")
    void 支付金额取订单快照() {
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.DOWN_PAYMENT.name()))
                .thenReturn(Optional.empty());
        when(paymentAdapter.createPayment(anyString(), any(), anyString()))
                .thenReturn(PaymentGatewayResult.builder()
                        .externalTradeNo("MOCK-P1").paymentMerchant("MOCK_PAY")
                        .paymentReference("mock://cashier/P1").gatewayStatus("PROCESSING").build());
        when(paymentRepository.findByPaymentNo(anyString())).thenAnswer(invocation -> {
            Payment p = Payment.builder()
                    .paymentNo(invocation.getArgument(0))
                    .orderId("OID1")
                    .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                    .paymentAmount(new BigDecimal("20000.00"))
                    .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                    .build();
            return Optional.of(p);
        });

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertNotNull(result.getPaymentNo());
        assertEquals(0, new BigDecimal("20000.00").compareTo(result.getPaymentAmount()));
        assertEquals(PaymentStatus.PROCESSING.name(), result.getPaymentStatus(), "Mock 受理后为处理中，不得直接成功");
        assertEquals("MOCK_PAY", result.getPaymentMerchant());
    }

    @Test
    @DisplayName("存在未过期活动支付单：返回原单且不重复调用网关")
    void 活动支付单直接返回() {
        Payment active = Payment.builder()
                .paymentNo("P-EXIST")
                .orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .externalTradeNo("MOCK-P-EXIST")
                .expireTime(LocalDateTime.now().plusMinutes(10))
                .build();
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.DOWN_PAYMENT.name()))
                .thenReturn(Optional.of(active));

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertEquals("P-EXIST", result.getPaymentNo());
        verify(paymentAdapter, never()).createPayment(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("过期 PENDING 支付单：先关闭再新建")
    void 过期支付单关闭并新建() {
        Payment expired = Payment.builder()
                .paymentNo("P-EXPIRED")
                .orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .expireTime(LocalDateTime.now().minusMinutes(1))
                .build();
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.DOWN_PAYMENT.name()))
                .thenReturn(Optional.of(expired));
        when(paymentAdapter.createPayment(anyString(), any(), anyString()))
                .thenReturn(PaymentGatewayResult.builder().externalTradeNo("MOCK-NEW").gatewayStatus("PROCESSING").build());
        when(paymentRepository.findByPaymentNo(anyString())).thenAnswer(invocation -> Optional.of(Payment.builder()
                .paymentNo(invocation.getArgument(0))
                .orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .build()));

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertEquals(PaymentStatus.CLOSED, expired.getPaymentStatus(), "过期支付单应先关闭");
        assertNotNull(result.getPaymentNo());
        assertTrue(result.getPaymentNo() != null && !"P-EXPIRED".equals(result.getPaymentNo()),
                "过期后应创建新支付单");
    }

    @Test
    @DisplayName("订单状态不允许发起支付：拒绝")
    void 订单状态不允许支付() {
        Order invalid = Order.builder().id("OID1").orderNo("SO001")
                .orderState(OrderState.DELIVERED).build();
        when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(invalid));

        assertThrows(OrderStateNotAllowedException.class, () ->
                paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("金额快照缺失：拒绝支付（禁止按当前价格补值）")
    void 金额快照缺失_拒绝() {
        when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.empty());

        assertThrows(OrderAmountSnapshotMissingException.class, () ->
                paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("网关异常：支付单标记失败并抛出支付冲突")
    void 网关异常标记失败() {
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.DOWN_PAYMENT.name()))
                .thenReturn(Optional.empty());
        when(paymentAdapter.createPayment(anyString(), any(), anyString()))
                .thenThrow(new RuntimeException("mock gateway down"));
        when(paymentRepository.findByPaymentNo(anyString())).thenAnswer(invocation -> Optional.of(Payment.builder()
                .paymentNo(invocation.getArgument(0))
                .orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .build()));

        assertThrows(PaymentConflictException.class, () ->
                paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("查询支付单：返回支付单号/阶段/金额/状态/时间")
    void 查询支付单() {
        Payment payment = Payment.builder()
                .paymentNo("P1").orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentChannel("WECHAT")
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PAID)
                .payTime(LocalDateTime.now())
                .externalTradeNo("MOCK-P1")
                .build();
        when(paymentRepository.findByPaymentNo("P1")).thenReturn(Optional.of(payment));
        when(orderRepository.findByOrderIdAndAccountId("OID1", "ACC1")).thenReturn(Optional.of(order));

        PaymentResult result = paymentAppService.getPayment("ACC1", "SO001", "P1");

        assertEquals("P1", result.getPaymentNo());
        assertEquals("SO001", result.getOrderNo());
        assertEquals(PaymentStatus.PAID.name(), result.getPaymentStatus());
        assertEquals(0, new BigDecimal("20000.00").compareTo(result.getPaymentAmount()));
    }

    @Test
    @DisplayName("MPT 按业务订单 ID 查询支付记录：不做账号隔离，不存在时返回 ORDER_NOT_EXIST")
    void MPT按订单ID查询支付记录() {
        Payment payment = Payment.builder()
                .paymentNo("P1").orderId("OID1")
                .paymentStage(PaymentStage.DOWN_PAYMENT.name())
                .paymentChannel("WECHAT")
                .paymentAmount(new BigDecimal("20000.00"))
                .paymentStatus(PaymentStatus.PAID)
                .build();
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId("OID1")).thenReturn(java.util.List.of(payment));

        java.util.List<PaymentResult> results = paymentAppService.listPaymentsByOrderId("OID1");

        assertEquals(1, results.size());
        assertEquals("P1", results.get(0).getPaymentNo());
        assertEquals("SO001", results.get(0).getOrderNo());
        assertEquals(PaymentStage.DOWN_PAYMENT.name(), results.get(0).getPaymentStage());
        verify(orderRepository, never()).findByOrderIdAndAccountId(anyString(), anyString());
    }

    @Test
    @DisplayName("MPT 支付查询：订单不存在返回 ORDER_NOT_EXIST")
    void MPT支付查询订单不存在() {
        when(orderRepository.findByOrderId("MISSING")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(OrderNotExistException.class,
                () -> paymentAppService.listPaymentsByOrderId("MISSING"));
    }

    private static void assertTrue(boolean condition, String message) {
        org.junit.jupiter.api.Assertions.assertTrue(condition, message);
    }
}
