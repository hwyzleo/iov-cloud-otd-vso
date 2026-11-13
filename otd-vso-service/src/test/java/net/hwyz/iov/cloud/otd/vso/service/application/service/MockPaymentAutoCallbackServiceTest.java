package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
 * Mock 支付渠道自回调测试（OTD-VSO-DSN-CR-007）。
 * <p>
 * 守护点：
 * <ol>
 *   <li>开关关闭时不得触发（生产安全默认）；</li>
 *   <li>只对 PROCESSING 支付单触发，终态/不存在一律跳过（幂等）；</li>
 *   <li>生成的签名字符串必须能被生产验签逻辑（{@link CallbackSignatureService#verifySignature}）通过
 *       —— 证明 mock 回调走的是与真实网关完全一致的签名与验签口径；</li>
 *   <li>回调命令字段（阶段/金额/外部单号/状态）与支付单一致，不得伪造金额。</li>
 * </ol>
 *
 * @author VSO Team
 */
class MockPaymentAutoCallbackServiceTest {

    private static final String SECRET = "hwyz1234567890abcdef";
    private static final String PAYMENT_NO = "PAY-001";
    private static final String EXTERNAL_TRADE_NO = "MOCK-" + PAYMENT_NO;

    private PaymentRepository paymentRepository;
    private PaymentCallbackService paymentCallbackService;
    private PaymentAdapter paymentAdapter;
    private CallbackSignatureService callbackSignatureService;
    private MockPaymentAutoCallbackService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        paymentCallbackService = mock(PaymentCallbackService.class);
        paymentAdapter = mock(PaymentAdapter.class);

        StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        callbackSignatureService = new CallbackSignatureService(redisTemplate);
        ReflectionTestUtils.setField(callbackSignatureService, "signatureSecret", SECRET);

        service = new MockPaymentAutoCallbackService(paymentRepository, callbackSignatureService,
                paymentCallbackService, paymentAdapter);
        ReflectionTestUtils.setField(service, "autoCallbackSetting", "");
    }

    private Payment payment(PaymentStatus status) {
        return Payment.builder()
                .paymentNo(PAYMENT_NO)
                .paymentStage("EARNEST_MONEY")
                .paymentAmount(new BigDecimal("5000.00"))
                .paymentStatus(status)
                .externalTradeNo(EXTERNAL_TRADE_NO)
                .build();
    }

    private PaymentCallbackResult okResult() {
        return PaymentCallbackResult.builder().code(PaymentCallbackResultCode.SUCCESS).message("成功").build();
    }

    @Test
    @DisplayName("真实渠道（非 mock）：默认不触发任何回调")
    void 非mock渠道_默认不触发() {
        when(paymentAdapter.isMock()).thenReturn(false);
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.of(payment(PaymentStatus.PROCESSING)));

        assertNull(service.triggerIfEnabled(PAYMENT_NO));
        verify(paymentCallbackService, never()).handleCallback(any(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("mock 渠道默认开启：PROCESSING 支付单生成合法签名的 PAID 回调并复用生产验签路径")
    void PROCESSING_生成合法签名回调() {
        when(paymentAdapter.isMock()).thenReturn(true);
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.of(payment(PaymentStatus.PROCESSING)));
        when(paymentCallbackService.handleCallback(any(), anyString(), anyLong(), anyString()))
                .thenReturn(okResult());

        PaymentCallbackResult result = service.triggerIfEnabled(PAYMENT_NO);
        assertNotNull(result);
        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());

        ArgumentCaptor<PaymentCallbackCmd> cmdCaptor = ArgumentCaptor.forClass(PaymentCallbackCmd.class);
        ArgumentCaptor<String> signatureCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> timestampCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> nonceCaptor = ArgumentCaptor.forClass(String.class);
        verify(paymentCallbackService).handleCallback(cmdCaptor.capture(), signatureCaptor.capture(),
                timestampCaptor.capture(), nonceCaptor.capture());

        PaymentCallbackCmd cmd = cmdCaptor.getValue();
        assertEquals(PAYMENT_NO, cmd.getPaymentNo());
        assertEquals(EXTERNAL_TRADE_NO, cmd.getExternalTradeNo());
        assertEquals("EARNEST_MONEY", cmd.getPaymentStage());
        assertEquals(0, new BigDecimal("5000.00").compareTo(cmd.getPaymentAmount()));
        assertEquals("PAID", cmd.getPaymentStatus());
        assertNotNull(cmd.getPayTime());
        assertNotNull(cmd.getIdempotentKey());

        // 关键断言：签名能被生产验签逻辑通过（与真实网关回调同一口径）
        Map<String, String> signData = new LinkedHashMap<>();
        signData.put("amount", cmd.getPaymentAmount().toPlainString());
        signData.put("orderId", cmd.getPaymentNo());
        signData.put("paySeq", cmd.getExternalTradeNo());
        signData.put("status", cmd.getPaymentStatus());
        assertTrue(callbackSignatureService.verifySignature(signatureCaptor.getValue(),
                        timestampCaptor.getValue(), nonceCaptor.getValue(), signData),
                "mock 自回调签名必须能通过生产验签");
    }

    @Test
    @DisplayName("显式关闭时 mock 渠道也不触发")
    void 显式关闭_mock也不触发() {
        when(paymentAdapter.isMock()).thenReturn(true);
        ReflectionTestUtils.setField(service, "autoCallbackSetting", "false");
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.of(payment(PaymentStatus.PROCESSING)));

        assertNull(service.triggerIfEnabled(PAYMENT_NO));
        verify(paymentCallbackService, never()).handleCallback(any(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("已终态支付单：跳过，不重复回调")
    void 终态_跳过() {
        when(paymentAdapter.isMock()).thenReturn(true);
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.of(payment(PaymentStatus.PAID)));

        assertNull(service.triggerIfEnabled(PAYMENT_NO));
        verify(paymentCallbackService, never()).handleCallback(any(), any(), anyLong(), eq("ignored"));
    }

    @Test
    @DisplayName("支付单不存在：跳过并告警，不抛异常")
    void 支付单不存在_跳过() {
        when(paymentAdapter.isMock()).thenReturn(true);
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.empty());
        assertNull(service.triggerIfEnabled(PAYMENT_NO));
        verify(paymentCallbackService, never()).handleCallback(any(), any(), anyLong(), anyString());
    }

    @Test
    @DisplayName("每次触发的 nonce 与幂等键唯一")
    void nonce与幂等键唯一() {
        when(paymentAdapter.isMock()).thenReturn(true);
        when(paymentRepository.findByPaymentNo(PAYMENT_NO)).thenReturn(Optional.of(payment(PaymentStatus.PROCESSING)));
        when(paymentCallbackService.handleCallback(any(), anyString(), anyLong(), anyString()))
                .thenReturn(okResult());

        service.triggerIfEnabled(PAYMENT_NO);
        service.triggerIfEnabled(PAYMENT_NO);

        ArgumentCaptor<String> nonceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> signatureCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> tsCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<PaymentCallbackCmd> cmdCaptor = ArgumentCaptor.forClass(PaymentCallbackCmd.class);
        verify(paymentCallbackService, org.mockito.Mockito.times(2))
                .handleCallback(cmdCaptor.capture(), signatureCaptor.capture(), tsCaptor.capture(), nonceCaptor.capture());

        assertEquals(2, nonceCaptor.getAllValues().stream().distinct().count(), "nonce 必须唯一");
        assertEquals(2, cmdCaptor.getAllValues().stream().map(PaymentCallbackCmd::getIdempotentKey).distinct().count(),
                "幂等键必须唯一");
        assertEquals(EXTERNAL_TRADE_NO, cmdCaptor.getAllValues().get(0).getExternalTradeNo());
    }
}
