package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ContractCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Mock 电子签服务端自回调测试（OTD-VSO-DSN-CR-014 §5.3）
 * <p>
 * 守护点：
 * <ol>
 *   <li>默认关闭，不得触发（生产安全默认）；</li>
 *   <li>只对 SIGNING 合同触发；</li>
 *   <li>生成的签名必须能被生产验签逻辑通过——证明自回调走与真实渠道一致的签名口径；</li>
 *   <li>回调命令字段与合同一致（合同号、签署任务号、SIGNED），不得伪造。</li>
 * </ol>
 *
 * @author VSO Team
 */
class MockContractAutoCallbackServiceTest {

    private static final String SECRET = "hwyz1234567890abcdef";
    private static final String CONTRACT_NO = "CT1";
    private static final String SIGN_NO = "SIGN1";

    private ContractRepository contractRepository;
    private CallbackSignatureService callbackSignatureService;
    private ContractCallbackService contractCallbackService;
    private MockContractAutoCallbackService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        contractRepository = mock(ContractRepository.class);
        contractCallbackService = mock(ContractCallbackService.class);
        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        callbackSignatureService = new CallbackSignatureService(redisTemplate);
        ReflectionTestUtils.setField(callbackSignatureService, "signatureSecret", SECRET);

        service = new MockContractAutoCallbackService(contractRepository, callbackSignatureService,
                contractCallbackService);
        when(contractCallbackService.handleCallback(any(), anyString(), anyLong(), anyString()))
                .thenReturn(PaymentCallbackResult.builder()
                        .code(PaymentCallbackResultCode.SUCCESS).message("ok").build());
    }

    private Contract signingContract() {
        Contract contract = Contract.draft("CID1", "OID1", CONTRACT_NO);
        contract.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        contract.markSigning(SIGN_NO);
        return contract;
    }

    @Test
    @DisplayName("默认关闭：不查询合同、不触发回调")
    void 默认关闭() {
        ReflectionTestUtils.setField(service, "autoCallbackEnabled", false);

        assertNull(service.triggerIfEnabled(CONTRACT_NO));
        verify(contractRepository, never()).findByContractNo(anyString());
        verify(contractCallbackService, never()).handleCallback(any(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("开启但合同非 SIGNING：跳过")
    void 非签署中跳过() {
        ReflectionTestUtils.setField(service, "autoCallbackEnabled", true);
        Contract generated = Contract.draft("CID1", "OID1", CONTRACT_NO);
        generated.markGenerated("MOCK-ESIGN-1", "mock://esign/contract/OID1/MOCK-ESIGN-1", LocalDateTime.now());
        when(contractRepository.findByContractNo(CONTRACT_NO)).thenReturn(Optional.of(generated));

        assertNull(service.triggerIfEnabled(CONTRACT_NO));
        verify(contractCallbackService, never()).handleCallback(any(), anyString(), anyLong(), anyString());
    }

    @Test
    @DisplayName("开启且 SIGNING：构造签名 SIGNED 回调，签名可被生产验签逻辑验证")
    void 签署中触发且签名可验证() {
        ReflectionTestUtils.setField(service, "autoCallbackEnabled", true);
        Contract contract = signingContract();
        when(contractRepository.findByContractNo(CONTRACT_NO)).thenReturn(Optional.of(contract));

        service.triggerIfEnabled(CONTRACT_NO);

        ArgumentCaptor<ContractCallbackCmd> cmdCaptor = ArgumentCaptor.forClass(ContractCallbackCmd.class);
        ArgumentCaptor<String> signatureCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> timestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> nonceCaptor = ArgumentCaptor.forClass(String.class);
        verify(contractCallbackService).handleCallback(cmdCaptor.capture(), signatureCaptor.capture(),
                timestampCaptor.capture(), nonceCaptor.capture());

        ContractCallbackCmd cmd = cmdCaptor.getValue();
        assertEquals(CONTRACT_NO, cmd.getContractNo());
        assertEquals(SIGN_NO, cmd.getSignNo());
        assertEquals(ContractStatus.SIGNED, cmd.getStatus());
        assertNotNull(cmd.getOccurredAt());
        assertNotNull(cmd.getIdempotentKey());

        Map<String, String> signData = Map.of(
                "contractNo", CONTRACT_NO,
                "signNo", SIGN_NO,
                "status", ContractStatus.SIGNED.name(),
                "occurredAt", cmd.getOccurredAt().toString());
        assertTrue(callbackSignatureService.verifySignature(signatureCaptor.getValue(),
                        timestampCaptor.getValue(), nonceCaptor.getValue(), signData),
                "Mock 自回调签名必须能被生产验签逻辑通过");
    }
}
