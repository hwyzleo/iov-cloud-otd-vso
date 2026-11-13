package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 处理中支付单失联判定测试（OTD-VSO-DSN-CR-005 US-018）
 *
 * @author VSO Team
 */
@DisplayName("支付单 - PROCESSING 失联关闭")
class PaymentStaleProcessingTest {

    private static final int STALE_MINUTES = 30;

    private Payment payment(PaymentStatus status, LocalDateTime expireTime) {
        return Payment.builder()
                .paymentId("PID1")
                .paymentNo("P1")
                .orderId("OID1")
                .paymentStage("EARNEST_MONEY")
                .paymentStatus(status)
                .expireTime(expireTime)
                .build();
    }

    @Test
    @DisplayName("超过 expire_time + 宽限期视为失联并可关闭")
    void 超期处理中支付单失联() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(STALE_MINUTES + 5);
        Payment payment = payment(PaymentStatus.PROCESSING, expireTime);

        assertTrue(payment.isStaleProcessing(LocalDateTime.now(), STALE_MINUTES));
        assertTrue(payment.closeIfStale(LocalDateTime.now(), STALE_MINUTES));
        assertEquals(PaymentStatus.CLOSED, payment.getPaymentStatus());
        assertEquals("支付处理中超时失联", payment.getFailReason());
    }

    @Test
    @DisplayName("宽限期内返回原单，不关闭")
    void 宽限期内不失联() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(STALE_MINUTES - 10);
        Payment payment = payment(PaymentStatus.PROCESSING, expireTime);

        assertFalse(payment.isStaleProcessing(LocalDateTime.now(), STALE_MINUTES));
        assertFalse(payment.closeIfStale(LocalDateTime.now(), STALE_MINUTES));
        assertEquals(PaymentStatus.PROCESSING, payment.getPaymentStatus());
        assertNull(payment.getFailReason());
    }

    @Test
    @DisplayName("PAID 永不关闭")
    void 已支付永不关闭() {
        Payment payment = payment(PaymentStatus.PAID, LocalDateTime.now().minusDays(1));

        assertFalse(payment.isStaleProcessing(LocalDateTime.now(), STALE_MINUTES));
        assertFalse(payment.closeIfStale(LocalDateTime.now(), STALE_MINUTES));
        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("PENDING_PAYMENT 走既有 closeExpired，不按失联处理")
    void 待支付不走失联() {
        Payment payment = payment(PaymentStatus.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(60));

        assertFalse(payment.isStaleProcessing(LocalDateTime.now(), STALE_MINUTES));
        assertFalse(payment.closeIfStale(LocalDateTime.now(), STALE_MINUTES));
        assertEquals(PaymentStatus.PENDING_PAYMENT, payment.getPaymentStatus());

        payment.closeExpired();
        assertEquals(PaymentStatus.CLOSED, payment.getPaymentStatus());
    }

    @Test
    @DisplayName("无 expire_time 时不判定失联")
    void 无截止时间不失联() {
        Payment payment = payment(PaymentStatus.PROCESSING, null);
        assertFalse(payment.isStaleProcessing(LocalDateTime.now(), STALE_MINUTES));
    }
}
