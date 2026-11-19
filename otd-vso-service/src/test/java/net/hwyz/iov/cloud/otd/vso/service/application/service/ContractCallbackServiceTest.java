package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ContractCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderStatusDimensionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 电子签回调应用服务测试（OTD-VSO-DSN-CR-014 §5.3 / INV-042 / INV-046）
 * <p>
 * 覆盖：验签失败、任务匹配、SIGNED/EFFECTIVE/INVALID 状态机、重复与冲突回调、
 * 合同 + 投影 + 时间线 + 回调日志的同事务写入调用。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("电子签回调 - 验签/幂等/状态机")
class ContractCallbackServiceTest {

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderStatusDimensionRepository orderStatusDimensionRepository;
    @Mock
    private CallbackLogRepository callbackLogRepository;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private CallbackSignatureService callbackSignatureService;

    private ContractCallbackService service;

    @BeforeEach
    void setUp() {
        service = new ContractCallbackService(contractRepository, orderRepository,
                orderStatusDimensionRepository, callbackLogRepository, orderTimelineAppService,
                callbackSignatureService, new ObjectMapper().findAndRegisterModules(), BusinessTimeZone.utc());
        lenient().when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(true);
        lenient().when(callbackLogRepository.save(any(CallbackLogPo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(contractRepository.save(any(Contract.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(orderStatusDimensionRepository.findByOrderId(anyString())).thenReturn(Optional.empty());
        lenient().when(orderStatusDimensionRepository.save(any(OrderStatusDimensionPo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(Order.builder()
                .id("OID1").orderNo("SO001").orderState(OrderState.PREPARE_DELIVER).build()));
    }

    private Contract signingContract() {
        Contract contract = Contract.draft("CID1", "OID1", "CT1");
        contract.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        contract.markSigning("SIGN1");
        return contract;
    }

    private ContractCallbackCmd cmd(ContractStatus status, String signNo, String idempotentKey) {
        return ContractCallbackCmd.builder()
                .contractNo("CT1")
                .signNo(signNo)
                .status(status)
                .occurredAt(Instant.now())
                .idempotentKey(idempotentKey)
                .build();
    }

    @Test
    @DisplayName("验签失败：拒绝更新合同事实并记录失败日志")
    void 验签失败拒绝() {
        when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(false);

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNED, "SIGN1", "ID1"),
                "bad-signature", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(contractRepository, never()).save(any(Contract.class));
        ArgumentCaptor<CallbackLogPo> captor = ArgumentCaptor.forClass(CallbackLogPo.class);
        verify(callbackLogRepository).save(captor.capture());
        assertEquals("FAIL", captor.getValue().getProcessResult());
    }

    @Test
    @DisplayName("签署任务不匹配：拒绝更新")
    void 签署任务不匹配拒绝() {
        when(contractRepository.findBySignNo("SIGN-OTHER")).thenReturn(Optional.of(signingContract()));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNED, "SIGN-OTHER", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("合法 SIGNED：合同置为 SIGNED，同事务写投影、时间线与成功日志")
    void 签署成功写合同投影时间线日志() {
        Contract contract = signingContract();
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(contract));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNED, "SIGN1", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals(ContractStatus.SIGNED, contract.getContractStatus());
        assertNotNull(contract.getSignTime());
        verify(contractRepository).save(contract);
        ArgumentCaptor<OrderStatusDimensionPo> projection = ArgumentCaptor.forClass(OrderStatusDimensionPo.class);
        verify(orderStatusDimensionRepository).save(projection.capture());
        assertEquals("SIGNED", projection.getValue().getContractStatus());
        assertEquals("OID1", projection.getValue().getOrderId());
        verify(orderTimelineAppService).record(eq("OID1"), eq(OrderTimelineEventType.CONTRACT_SIGNED),
                eq("600"), eq("600"), anyString(), anyString(), anyString(), eq("SIGN1"), any(), anyString());
        ArgumentCaptor<CallbackLogPo> log = ArgumentCaptor.forClass(CallbackLogPo.class);
        verify(callbackLogRepository).save(log.capture());
        assertEquals("CONTRACT", log.getValue().getBusinessType());
        assertEquals("MOCK_ESIGN", log.getValue().getExternalSystemName());
        assertEquals("SUCCESS", log.getValue().getProcessResult());
    }

    @Test
    @DisplayName("重复 SIGNED：幂等返回，不重复追加成功时间线与金额事实")
    void 重复签署幂等() {
        Contract contract = signingContract();
        contract.markSigned(LocalDateTime.now());
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(contract));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNED, "SIGN1", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.DUPLICATE, result.getCode());
        verify(contractRepository, never()).save(any(Contract.class));
        verify(orderTimelineAppService, never()).record(anyString(), any(), any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    @Test
    @DisplayName("SIGNED 后 INVALID：冲突拒绝，不回退合同")
    void 签署后失效冲突拒绝() {
        Contract contract = signingContract();
        contract.markSigned(LocalDateTime.now());
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(contract));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.INVALID, "SIGN1", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.CONFLICT, result.getCode());
        assertEquals(ContractStatus.SIGNED, contract.getContractStatus());
        verify(contractRepository, never()).save(any(Contract.class));
    }

    @Test
    @DisplayName("SIGNING 回调重复：幂等返回")
    void 签署中回调幂等() {
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(signingContract()));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNING, "SIGN1", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.DUPLICATE, result.getCode());
    }

    @Test
    @DisplayName("SIGNED 后合法 EFFECTIVE：合同生效，不重复追加签署时间线")
    void 签署后合法生效() {
        Contract contract = signingContract();
        contract.markSigned(LocalDateTime.now());
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(contract));

        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.EFFECTIVE, "SIGN1", "ID1"),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals(ContractStatus.EFFECTIVE, contract.getContractStatus());
        assertNotNull(contract.getEffectiveTime());
        verify(contractRepository).save(contract);
        verify(orderTimelineAppService, never()).record(anyString(), any(), any(), any(), any(), any(), any(),
                any(), any(), any());
    }

    @Test
    @DisplayName("SIGNING 收到 INVALID：合同失效并写失效时间线")
    void 签署中失效() {
        Contract contract = signingContract();
        when(contractRepository.findBySignNo("SIGN1")).thenReturn(Optional.of(contract));

        PaymentCallbackResult result = service.handleCallback(ContractCallbackCmd.builder()
                        .contractNo("CT1").signNo("SIGN1").status(ContractStatus.INVALID)
                        .occurredAt(Instant.now()).idempotentKey("ID1").failReason("渠道拒绝").build(),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals(ContractStatus.INVALID, contract.getContractStatus());
        assertEquals("渠道拒绝", contract.getInvalidReason());
        verify(orderTimelineAppService).record(eq("OID1"), eq(OrderTimelineEventType.CONTRACT_INVALID),
                eq("600"), eq("600"), anyString(), anyString(), anyString(), eq("SIGN1"), eq("渠道拒绝"), anyString());
    }

    @Test
    @DisplayName("缺失幂等键：拒绝处理")
    void 缺失幂等键拒绝() {
        PaymentCallbackResult result = service.handleCallback(cmd(ContractStatus.SIGNED, "SIGN1", null),
                "sig", 100L, "nonce1");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(contractRepository, never()).findBySignNo(anyString());
    }
}
