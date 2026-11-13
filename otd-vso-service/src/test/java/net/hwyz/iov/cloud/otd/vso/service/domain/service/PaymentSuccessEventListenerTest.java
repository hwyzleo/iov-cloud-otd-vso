package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * 支付成功事件监听测试（OTD-VSO-DSN-CR-005）
 * <p>
 * 监听器只负责取消小订单支付超时任务，不再推进订单状态。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("支付成功监听 - 仅取消超时任务")
class PaymentSuccessEventListenerTest {

    @Mock
    private TimeoutNotifyService timeoutNotifyService;

    @InjectMocks
    private PaymentSuccessEventListener eventListener;

    private PaymentSuccessDomainEvent event;

    @BeforeEach
    void setUp() {
        event = PaymentSuccessDomainEvent.builder()
                .orderId("order_001")
                .paymentId("payment_001")
                .paymentStage(PaymentStage.EARNEST_MONEY)
                .paymentAmount(new BigDecimal("5000.00"))
                .payTime(LocalDateTime.now())
                .occurTime(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("支付成功取消 SMALL_ORDER_PAY_TIMEOUT 任务")
    void 取消超时任务() {
        eventListener.handlePaymentSuccess(event);

        verify(timeoutNotifyService)
                .cancelByOrderIdAndType(eq("order_001"), eq("SMALL_ORDER_PAY_TIMEOUT"));
    }

    @Test
    @DisplayName("空事件不触发任务取消")
    void 空事件忽略() {
        eventListener.handlePaymentSuccess(null);
        eventListener.handlePaymentSuccess(PaymentSuccessDomainEvent.builder().build());

        verify(timeoutNotifyService, never()).cancelByOrderIdAndType(anyString(), anyString());
    }

    @Test
    @DisplayName("取消失败只告警，不抛出异常影响支付结果")
    void 取消失败不抛异常() {
        doThrow(new RuntimeException("redis down"))
                .when(timeoutNotifyService).cancelByOrderIdAndType(anyString(), anyString());

        eventListener.handlePaymentSuccess(event);
    }
}
