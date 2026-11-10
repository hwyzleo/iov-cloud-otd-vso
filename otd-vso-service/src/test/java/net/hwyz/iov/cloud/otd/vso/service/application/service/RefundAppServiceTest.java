package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.RefundAmountInvalidException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.RefundGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 退款申请测试（US-006 / §14：多支付分摊、活动退款、金额异常、重试）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("退款申请 - 多支付分摊/幂等/重试")
class RefundAppServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private RefundItemRepository refundItemRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private PaymentAdapter paymentAdapter;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private RefundAppService refundAppService;

    private Order order;
    private OrderAmount orderAmount;
    private Payment earnestPayment;
    private Payment supplementPayment;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .build();
        orderAmount = new OrderAmount("AMT1");
        orderAmount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        orderAmount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        orderAmount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        orderAmount.setRefundTotal(new Money(BigDecimal.ZERO));

        earnestPayment = Payment.builder().paymentId("PID-E").paymentNo("P-EARNEST")
                .orderId("OID1").paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus.PAID)
                .payTime(LocalDateTime.now().minusMinutes(10)).build();
        supplementPayment = Payment.builder().paymentId("PID-S").paymentNo("P-SUPPLEMENT")
                .orderId("OID1").paymentAmount(new BigDecimal("15000.00"))
                .paymentStatus(net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus.PAID)
                .payTime(LocalDateTime.now()).build();

        lenient().when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));
        lenient().when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.of(orderAmount));
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
        lenient().when(transactionManager.getTransaction(any()))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        lenient().when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(paymentAdapter.refundItem(anyString(), anyString(), any(), anyString()))
                .thenAnswer(invocation -> RefundGatewayResult.builder()
                        .externalRefundNo("MOCK-" + invocation.getArgument(0))
                        .gatewayStatus("processing").build());
    }

    @Test
    @DisplayName("§8.6 两笔支付分摊：P-EARNEST 5000 + P-SUPPLEMENT 15000")
    void 多支付分摊() {
        when(refundRepository.findActiveByOrderId("OID1")).thenReturn(Optional.empty());
        when(paymentRepository.findSuccessfulByOrderId("OID1")).thenReturn(List.of(earnestPayment, supplementPayment));
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(refundRepository.findByRefundNo(anyString())).thenAnswer(invocation -> Optional.of(
                capturedRefund()));
        when(refundItemRepository.findByRefundId(anyString())).thenAnswer(invocation -> capturedRefund().getItems());

        RefundResult result = refundAppService.apply("ACC1", "SO001");

        assertEquals(2, result.getItems().size(), "应拆分为两笔退款明细");
        assertEquals("P-EARNEST", result.getItems().get(0).getPaymentNo());
        assertEquals("P-SUPPLEMENT", result.getItems().get(1).getPaymentNo());
        assertEquals(0, new BigDecimal("5000.00").compareTo(result.getItems().get(0).getRefundAmount()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(result.getItems().get(1).getRefundAmount()));
        assertEquals(0, new BigDecimal("20000.00").compareTo(result.getRefundAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getRefundFee()), "未锁单退款无手续费");
    }

    @Test
    @DisplayName("已存在活动退款：幂等返回，不重复创建")
    void 活动退款幂等() {
        Refund existing = refund("RF-EXIST", RefundStatus.PROCESSING);
        when(refundRepository.findActiveByOrderId("OID1")).thenReturn(Optional.of(existing));

        RefundResult result = refundAppService.apply("ACC1", "SO001");

        assertEquals("RF-EXIST", result.getRefundNo());
        verify(refundRepository, atLeastOnce()).findActiveByOrderId("OID1");
    }

    @Test
    @DisplayName("退款金额为 0：拒绝申请")
    void 退款金额为零_拒绝() {
        orderAmount.setPaidTotal(new Money(BigDecimal.ZERO));
        when(refundRepository.findActiveByOrderId("OID1")).thenReturn(Optional.empty());

        assertThrows(RefundAmountInvalidException.class, () -> refundAppService.apply("ACC1", "SO001"));
    }

    @Test
    @DisplayName("失败明细重试：仅重发 failed/pending 明细")
    void 重试仅处理失败明细() {
        Refund refund = refund("RF-RETRY", RefundStatus.FAILED);
        when(refundRepository.findByRefundNo("RF-RETRY")).thenReturn(Optional.of(refund));
        when(orderRepository.findByOrderIdAndAccountId("OID1", "ACC1")).thenReturn(Optional.of(order));
        when(refundItemRepository.findByRefundItemId(anyString()))
                .thenAnswer(invocation -> refund.getItems().stream()
                        .filter(item -> item.getRefundItemId().equals(invocation.getArgument(0)))
                        .findFirst());
        when(refundItemRepository.findByRefundId(anyString())).thenReturn(refund.getItems());
        when(refundRepository.findByRefundNo("RF-RETRY")).thenReturn(Optional.of(refund));

        RefundResult result = refundAppService.retry("ACC1", "RF-RETRY");

        assertEquals("RF-RETRY", result.getRefundNo());
        verify(paymentAdapter, atLeastOnce()).refundItem(anyString(), anyString(), any(), anyString());
    }

    private Refund capturedRefund() {
        ArgumentCaptor<Refund> captor = ArgumentCaptor.forClass(Refund.class);
        verify(refundRepository, atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("MPT 按业务订单 ID 查询退款记录：返回退款场景与手续费（不做账号隔离）")
    void MPT按订单ID查询退款() {
        Refund refund = Refund.builder()
                .refundId("RID-REF1")
                .refundNo("REF1")
                .orderId("OID1")
                .refundScene("arrange_production")
                .refundAmount(new BigDecimal("19000.00"))
                .refundFee(new BigDecimal("1000.00"))
                .refundStatus(RefundStatus.PENDING)
                .applyTime(LocalDateTime.now())
                .items(new java.util.ArrayList<>(List.of(
                        RefundItem.builder().refundItemId("RI-1").refundId("RID-REF1")
                                .paymentId("PID-E").paymentNo("P-EARNEST")
                                .refundAmount(new BigDecimal("4000.00"))
                                .refundStatus(RefundStatus.PROCESSING).build(),
                        RefundItem.builder().refundItemId("RI-2").refundId("RID-REF1")
                                .paymentId("PID-S").paymentNo("P-SUPPLEMENT")
                                .refundAmount(new BigDecimal("15000.00"))
                                .refundStatus(RefundStatus.PROCESSING).build())))
                .build();
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(Order.builder()
                .id("OID1").orderNo("SO001").orderState(OrderState.ARRANGE_PRODUCTION).build()));
        when(refundRepository.findByOrderId("OID1")).thenReturn(List.of(refund));

        List<RefundResult> results = refundAppService.listByOrderId("OID1");

        assertEquals(1, results.size());
        RefundResult result = results.get(0);
        assertEquals("REF1", result.getRefundNo());
        assertEquals("arrange_production", result.getRefundScene(), "必须返回退款场景以便核对手续费规则");
        assertEquals(0, new BigDecimal("1000.00").compareTo(result.getRefundFee()));
        assertEquals(2, result.getItems().size());
        assertEquals(0, new BigDecimal("20000.00").compareTo(
                result.getRefundAmount().add(result.getRefundFee())), "退款金额+手续费应等于申请时已支付金额");
    }

    @Test
    @DisplayName("MPT 退款查询：订单不存在返回 ORDER_NOT_EXIST")
    void MPT退款查询订单不存在() {
        when(orderRepository.findByOrderId("MISSING")).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class, () -> refundAppService.listByOrderId("MISSING"));
    }

    private Refund refund(String refundNo, RefundStatus status) {
        return Refund.builder()
                .refundId("RID-" + refundNo)
                .refundNo(refundNo)
                .orderId("OID1")
                .refundAmount(new BigDecimal("20000.00"))
                .refundFee(BigDecimal.ZERO)
                .refundStatus(status)
                .applyTime(LocalDateTime.now())
                .items(new java.util.ArrayList<>(List.of(
                        RefundItem.builder().refundItemId("RI-1").refundId("RID-" + refundNo)
                                .paymentId("PID-E").paymentNo("P-EARNEST")
                                .refundAmount(new BigDecimal("5000.00"))
                                .refundStatus(status == RefundStatus.FAILED ? RefundStatus.FAILED : RefundStatus.PROCESSING)
                                .build(),
                        RefundItem.builder().refundItemId("RI-2").refundId("RID-" + refundNo)
                                .paymentId("PID-S").paymentNo("P-SUPPLEMENT")
                                .refundAmount(new BigDecimal("15000.00"))
                                .refundStatus(RefundStatus.FAILED)
                                .build())))
                .build();
    }
}
