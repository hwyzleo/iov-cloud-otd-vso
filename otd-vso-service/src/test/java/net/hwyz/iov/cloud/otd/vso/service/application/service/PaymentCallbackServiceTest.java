package net.hwyz.iov.cloud.otd.vso.service.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCallbackServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CallbackLogRepository callbackLogRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PaymentCallbackService paymentCallbackService;

    private PaymentCallbackCmd validCmd;
    private PaymentPo paymentPo;

    @BeforeEach
    void setUp() {
        validCmd = PaymentCallbackCmd.builder()
                .paymentNo("PAY20240101001")
                .externalTradeNo("EXT20240101001")
                .paymentStage(PaymentStage.EARNEST_MONEY.name())
                .paymentAmount(new BigDecimal("1000.00"))
                .paymentStatus(PaymentStatus.PAID.name())
                .payTime(LocalDateTime.now())
                .idempotentKey("PAY:EXT20240101001")
                .build();

        paymentPo = new PaymentPo();
        paymentPo.setPaymentId("payment_001");
        paymentPo.setPaymentNo("PAY20240101001");
        paymentPo.setOrderId("order_001");
        paymentPo.setPaymentStage(PaymentStage.EARNEST_MONEY.name());
        paymentPo.setPaymentAmount(new BigDecimal("1000.00"));
        paymentPo.setPaymentStatus(PaymentStatus.PENDING_PAYMENT.name());
    }

    @Test
    void testHandleCallback_Success() {
        when(callbackLogRepository.findByIdempotentKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findByPaymentNo("PAY20240101001")).thenReturn(Optional.of(paymentPo));
        when(callbackLogRepository.save(any(CallbackLogPo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(validCmd);

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals("回调处理成功", result.getMessage());

        verify(callbackLogRepository, times(2)).save(any(CallbackLogPo.class));
        verify(paymentRepository).updateStatus(
                eq("PAY20240101001"),
                eq(PaymentStatus.PAID.name()),
                eq("EXT20240101001"),
                any(LocalDateTime.class)
        );

        ArgumentCaptor<PaymentSuccessDomainEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessDomainEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        PaymentSuccessDomainEvent event = eventCaptor.getValue();
        assertEquals("order_001", event.getOrderId());
        assertEquals("payment_001", event.getPaymentId());
        assertEquals(PaymentStage.EARNEST_MONEY, event.getPaymentStage());
        assertEquals(new BigDecimal("1000.00"), event.getPaymentAmount());
    }

    @Test
    void testHandleCallback_Duplicate() {
        CallbackLogPo existingLog = new CallbackLogPo();
        existingLog.setIdempotentKey("PAY:EXT20240101001");
        existingLog.setProcessResult("SUCCESS");

        when(callbackLogRepository.findByIdempotentKey("PAY:EXT20240101001")).thenReturn(Optional.of(existingLog));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(validCmd);

        assertEquals(PaymentCallbackResultCode.DUPLICATE, result.getCode());
        assertEquals("该回调已处理", result.getMessage());

        verify(paymentRepository, never()).findByPaymentNo(any());
        verify(paymentRepository, never()).updateStatus(any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleCallback_PaymentNotExist() {
        when(callbackLogRepository.findByIdempotentKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findByPaymentNo("PAY20240101001")).thenReturn(Optional.empty());
        when(callbackLogRepository.save(any(CallbackLogPo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(validCmd);

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        assertEquals("支付单不存在", result.getMessage());

        verify(callbackLogRepository).save(any(CallbackLogPo.class));
        verify(paymentRepository, never()).updateStatus(any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleCallback_StatusMismatch() {
        paymentPo.setPaymentStatus(PaymentStatus.PAID.name());

        when(callbackLogRepository.findByIdempotentKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findByPaymentNo("PAY20240101001")).thenReturn(Optional.of(paymentPo));
        when(callbackLogRepository.save(any(CallbackLogPo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(validCmd);

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        assertTrue(result.getMessage().contains("支付单状态不匹配"));

        verify(callbackLogRepository).save(any(CallbackLogPo.class));
        verify(paymentRepository, never()).updateStatus(any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleCallback_AmountMismatch() {
        paymentPo.setPaymentAmount(new BigDecimal("2000.00"));

        when(callbackLogRepository.findByIdempotentKey(any())).thenReturn(Optional.empty());
        when(paymentRepository.findByPaymentNo("PAY20240101001")).thenReturn(Optional.of(paymentPo));
        when(callbackLogRepository.save(any(CallbackLogPo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentCallbackResult result = paymentCallbackService.handleCallback(validCmd);

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        assertTrue(result.getMessage().contains("金额不一致"));

        verify(callbackLogRepository).save(any(CallbackLogPo.class));
        verify(paymentRepository, never()).updateStatus(any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}