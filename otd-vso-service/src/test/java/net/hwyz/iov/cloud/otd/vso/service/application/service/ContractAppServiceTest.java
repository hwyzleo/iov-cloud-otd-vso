package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiateContractSignCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractSignResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ContractStatusNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.EsignGatewayFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.TailPaymentNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.ContractGenerationResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignSignStatusResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.SignTaskResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 购车合同应用服务测试（OTD-VSO-DSN-CR-014 US-032）
 * <p>
 * 覆盖：首次生成、各状态续签、SIGNING 同 signNo、SIGNED 幂等、
 * 非本人/非 600/贷款拒绝、Adapter 失败不伪成功。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("购车合同签署 - 分阶段持久化与幂等")
class ContractAppServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EsignAdapter esignAdapter;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private MockContractAutoCallbackService mockContractAutoCallbackService;
    @Mock
    private PlatformTransactionManager transactionManager;

    private ContractAppService contractAppService;

    private final AtomicReference<Contract> current = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        contractAppService = new ContractAppService(orderRepository, contractRepository, esignAdapter,
                orderLockService, orderTimelineAppService, mockContractAutoCallbackService,
                transactionManager, BusinessTimeZone.utc());
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
        lenient().when(transactionManager.getTransaction(any()))
                .thenAnswer(invocation -> new SimpleTransactionStatus());
        lenient().when(contractRepository.findByOrderIdAndType(anyString(), anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(current.get()));
        lenient().when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> {
            Contract saved = invocation.getArgument(0);
            current.set(saved);
            return saved;
        });
    }

    private Order order(OrderState state, String paymentMethod) {
        return Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderState(state)
                .paymentMethod(paymentMethod)
                .build();
    }

    private void givenOrder(Order order) {
        when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));
    }

    @Test
    @DisplayName("首次发起：DRAFT → GENERATED → SIGNING，返回合同号与签署任务号")
    void 首次发起生成并签署() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "full_payment"));
        when(esignAdapter.generateContract(anyString(), anyString(), any()))
                .thenReturn(ContractGenerationResult.builder()
                        .externalContractNo("MOCK-ESIGN-1")
                        .fileUrl("mock://esign/contract/OID1/MOCK-ESIGN-1")
                        .generateTime(LocalDateTime.now())
                        .build());
        when(esignAdapter.createSignTask(anyString(), anyString()))
                .thenReturn(SignTaskResult.builder().signNo("SIGN1").signUrl("mock://esign/sign/SIGN1").build());

        ContractSignResult result = contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId("ACC1").orderNo("SO001").build());

        assertEquals(ContractStatus.SIGNING.name(), result.getContractStatus());
        assertEquals("SIGN1", result.getSignNo());
        assertEquals("mock://esign/sign/SIGN1", result.getSignUrl());
        assertNotNull(result.getContractNo());
        assertEquals("MOCK-ESIGN-1", current.get().getExternalContractNo());
        assertEquals("SIGN1", current.get().getSignNo());
    }

    @Test
    @DisplayName("SIGNING 重复发起：复用当前 signNo，不重复创建签署任务")
    void 签署中重复发起复用任务() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "full_payment"));
        Contract signing = Contract.draft("CID1", "OID1", "CT1");
        signing.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        signing.markSigning("SIGN1");
        current.set(signing);
        when(esignAdapter.querySignStatus("SIGN1"))
                .thenReturn(EsignSignStatusResult.builder().signNo("SIGN1")
                        .status(ContractStatus.SIGNING).signUrl("mock://esign/sign/SIGN1").build());

        ContractSignResult result = contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId("ACC1").orderNo("SO001").build());

        assertEquals("SIGN1", result.getSignNo());
        verify(esignAdapter, never()).generateContract(anyString(), anyString(), any());
        verify(esignAdapter, never()).createSignTask(anyString(), anyString());
    }

    @Test
    @DisplayName("SIGNED 终态：幂等返回当前合同事实，不新建合同或签署任务")
    void 已签署幂等返回() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "full_payment"));
        Contract signed = Contract.draft("CID1", "OID1", "CT1");
        signed.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        signed.markSigning("SIGN1");
        signed.markSigned(LocalDateTime.now());
        current.set(signed);

        ContractSignResult result = contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId("ACC1").orderNo("SO001").build());

        assertEquals(ContractStatus.SIGNED.name(), result.getContractStatus());
        assertNull(result.getSignUrl());
        verify(esignAdapter, never()).generateContract(anyString(), anyString(), any());
        verify(esignAdapter, never()).createSignTask(anyString(), anyString());
    }

    @Test
    @DisplayName("INVALID 后重新发起：跳过生成，直接创建新的签署任务")
    void 失效后重新发起签署() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "full_payment"));
        Contract invalid = Contract.draft("CID1", "OID1", "CT1");
        invalid.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        invalid.markSigning("SIGN1");
        invalid.markInvalid(LocalDateTime.now(), "渠道失效");
        current.set(invalid);
        when(esignAdapter.createSignTask(anyString(), anyString()))
                .thenReturn(SignTaskResult.builder().signNo("SIGN2").signUrl("mock://esign/sign/SIGN2").build());

        ContractSignResult result = contractAppService.initiateSign(InitiateContractSignCmd.builder()
                .accountId("ACC1").orderNo("SO001").build());

        assertEquals("SIGN2", result.getSignNo());
        assertEquals(ContractStatus.SIGNING.name(), result.getContractStatus());
        verify(esignAdapter, never()).generateContract(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("他人订单：按 ORDER_NOT_EXIST 处理")
    void 他人订单不存在() {
        when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class, () -> contractAppService.initiateSign(
                InitiateContractSignCmd.builder().accountId("ACC1").orderNo("SO001").build()));
    }

    @Test
    @DisplayName("非 600 订单：拒绝发起合同签署")
    void 非待交付订单拒绝() {
        givenOrder(order(OrderState.TRANSPORTING, "full_payment"));

        assertThrows(ContractStatusNotAllowedException.class, () -> contractAppService.initiateSign(
                InitiateContractSignCmd.builder().accountId("ACC1").orderNo("SO001").build()));
        verify(esignAdapter, never()).generateContract(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("贷款订单：拒绝发起合同签署")
    void 贷款订单拒绝() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "loan"));

        assertThrows(TailPaymentNotAllowedException.class, () -> contractAppService.initiateSign(
                InitiateContractSignCmd.builder().accountId("ACC1").orderNo("SO001").build()));
    }

    @Test
    @DisplayName("Adapter 生成失败：返回 201063，不伪造签署成功")
    void 生成失败不伪成功() {
        givenOrder(order(OrderState.PREPARE_DELIVER, "full_payment"));
        when(esignAdapter.generateContract(anyString(), anyString(), any()))
                .thenThrow(new IllegalStateException("mocked outage"));

        assertThrows(EsignGatewayFailedException.class, () -> contractAppService.initiateSign(
                InitiateContractSignCmd.builder().accountId("ACC1").orderNo("SO001").build()));
        assertEquals(ContractStatus.DRAFT, current.get().getContractStatus());
    }
}
