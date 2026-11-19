package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.TailPaymentNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 全款订单尾款发起测试（OTD-VSO-DSN-CR-014 US-033）
 * <p>
 * 覆盖：未签/签署中拒绝、SIGNED/EFFECTIVE 放行、金额快照、paid/unpaid 一致性、
 * 贷款/非 600 拒绝、活动单复用、响应 paymentStage。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("尾款发起 - 合同门槛/快照/阶段")
class PaymentAppServiceTailTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private PaymentAdapter paymentAdapter;
    @Mock
    private PaymentChannelConfig paymentChannelConfig;
    @Mock
    private PlatformTransactionManager transactionManager;
    @Mock
    private MockPaymentAutoCallbackService mockPaymentAutoCallbackService;

    @InjectMocks
    private PaymentAppService paymentAppService;

    private Order order;
    private OrderAmount amount;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(OrderState.PREPARE_DELIVER)
                .paymentMethod("full_payment")
                .build();
        amount = new OrderAmount("AMT1");
        amount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        amount.setNetReceivableTotal(new Money(new BigDecimal("203000.00")));
        amount.setUnpaidTotal(new Money(new BigDecimal("183000.00")));
        amount.setTailPaymentAmount(new Money(new BigDecimal("183000.00")));

        lenient().when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));
        lenient().when(orderAmountRepository.findDomainByOrderId("OID1")).thenReturn(Optional.of(amount));
        lenient().when(paymentChannelConfig.isChannelEnabled(PaymentChannel.WECHAT)).thenReturn(true);
        lenient().when(paymentChannelConfig.getDownPaymentTimeoutMinutes()).thenReturn(60);
        lenient().when(paymentChannelConfig.tailPaymentTimeoutMinutesOrDefault()).thenReturn(60);
        lenient().when(paymentChannelConfig.processingStaleMinutesOrDefault()).thenReturn(30);
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
        lenient().when(transactionManager.getTransaction(any()))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        lenient().when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Contract contract(ContractStatus status) {
        Contract contract = Contract.draft("CID1", "OID1", "CT1");
        if (status == ContractStatus.SIGNED || status == ContractStatus.EFFECTIVE) {
            contract.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
            contract.markSigning("SIGN1");
            contract.markSigned(LocalDateTime.now());
        }
        if (status == ContractStatus.EFFECTIVE) {
            contract.markEffective(LocalDateTime.now());
        }
        if (status == ContractStatus.SIGNING) {
            contract.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
            contract.markSigning("SIGN1");
        }
        return contract;
    }

    private void mockGatewayAndReload(BigDecimal amount) {
        when(paymentAdapter.createPayment(anyString(), any(), anyString()))
                .thenReturn(PaymentGatewayResult.builder()
                        .externalTradeNo("MOCK-T1").paymentMerchant("MOCK_PAY")
                        .paymentReference("mock://cashier/T1").gatewayStatus("PROCESSING").build());
        when(paymentRepository.findByPaymentNo(anyString())).thenAnswer(invocation -> Optional.of(Payment.builder()
                .paymentNo(invocation.getArgument(0))
                .orderId("OID1")
                .paymentStage(PaymentStage.TAIL_PAYMENT.name())
                .paymentAmount(amount)
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .build()));
    }

    @Test
    @DisplayName("合同已签署：600 发起尾款，金额取快照 183000，返回 TAIL_PAYMENT")
    void 合同已签署发起尾款() {
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNED)));
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.TAIL_PAYMENT.name()))
                .thenReturn(Optional.empty());
        mockGatewayAndReload(new BigDecimal("183000.00"));

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertEquals(PaymentStage.TAIL_PAYMENT.name(), result.getPaymentStage());
        assertEquals(0, new BigDecimal("183000.00").compareTo(result.getPaymentAmount()));
    }

    @Test
    @DisplayName("合同已生效：允许发起尾款")
    void 合同已生效发起尾款() {
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.EFFECTIVE)));
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.TAIL_PAYMENT.name()))
                .thenReturn(Optional.empty());
        mockGatewayAndReload(new BigDecimal("183000.00"));

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertEquals(PaymentStage.TAIL_PAYMENT.name(), result.getPaymentStage());
    }

    @Test
    @DisplayName("合同未签署：返回 TAIL_PAYMENT_NOT_ALLOWED，不创建支付单")
    void 合同未签署拒绝() {
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.empty());

        TailPaymentNotAllowedException exception = assertThrows(TailPaymentNotAllowedException.class,
                () -> paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
        assertEquals("201064", exception.getErrorCode().getCode());
        verify(paymentAdapter, never()).createPayment(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("合同签署中：拒绝发起尾款")
    void 合同签署中拒绝() {
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNING)));

        assertThrows(TailPaymentNotAllowedException.class, () -> paymentAppService.initiatePayment(
                InitiatePaymentCmd.builder().accountId("ACC1").orderNo("SO001")
                        .paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("贷款订单：拒绝发起尾款")
    void 贷款订单拒绝() {
        order.savePaymentMethod("loan");

        TailPaymentNotAllowedException exception = assertThrows(TailPaymentNotAllowedException.class,
                () -> paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
        assertEquals("201064", exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("尾款快照为 0：按快照异常拒绝（人工修复）")
    void 尾款快照为零拒绝() {
        amount.setTailPaymentAmount(new Money(BigDecimal.ZERO));
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNED)));

        OrderAmountSnapshotMissingException exception = assertThrows(OrderAmountSnapshotMissingException.class,
                () -> paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                        .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build()));
        assertEquals("201050", exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("已支付金额与首付款不一致：拒绝尾款")
    void 已支付金额不一致拒绝() {
        amount.setPaidTotal(new Money(new BigDecimal("25000.00")));
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNED)));

        assertThrows(TailPaymentNotAllowedException.class, () -> paymentAppService.initiatePayment(
                InitiatePaymentCmd.builder().accountId("ACC1").orderNo("SO001")
                        .paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("待支付金额与尾款快照不一致：按快照异常拒绝")
    void 待支付金额不一致拒绝() {
        amount.setUnpaidTotal(new Money(new BigDecimal("183001.00")));
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNED)));

        assertThrows(OrderAmountSnapshotMissingException.class, () -> paymentAppService.initiatePayment(
                InitiatePaymentCmd.builder().accountId("ACC1").orderNo("SO001")
                        .paymentChannel(PaymentChannel.WECHAT).build()));
    }

    @Test
    @DisplayName("存在未过期尾款活动单：复用原单且不重复调用网关")
    void 尾款活动单复用() {
        when(contractRepository.findByOrderIdAndType("OID1", Contract.TYPE_VEHICLE_PURCHASE))
                .thenReturn(Optional.of(contract(ContractStatus.SIGNED)));
        Payment active = Payment.builder()
                .paymentNo("P-TAIL")
                .orderId("OID1")
                .paymentStage(PaymentStage.TAIL_PAYMENT.name())
                .paymentAmount(new BigDecimal("183000.00"))
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .externalTradeNo("MOCK-P-TAIL")
                .expireTime(LocalDateTime.now().plusMinutes(30))
                .build();
        when(paymentRepository.findActiveByOrderIdAndStage("OID1", PaymentStage.TAIL_PAYMENT.name()))
                .thenReturn(Optional.of(active));

        InitiatePaymentResult result = paymentAppService.initiatePayment(InitiatePaymentCmd.builder()
                .accountId("ACC1").orderNo("SO001").paymentChannel(PaymentChannel.WECHAT).build());

        assertEquals("P-TAIL", result.getPaymentNo());
        assertEquals(PaymentStage.TAIL_PAYMENT.name(), result.getPaymentStage());
        verify(paymentAdapter, never()).createPayment(anyString(), any(), anyString());
    }
}
