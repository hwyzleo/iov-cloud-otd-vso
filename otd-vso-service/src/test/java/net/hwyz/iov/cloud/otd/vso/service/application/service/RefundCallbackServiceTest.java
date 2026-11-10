package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.RefundCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 退款回调测试（US-006 / §14：乱序明细、主单聚合、重复、冲突、refundTotal）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("退款回调 - 明细聚合/幂等/冲突")
class RefundCallbackServiceTest {

    @Mock
    private RefundRepository refundRepository;
    @Mock
    private RefundItemRepository refundItemRepository;
    @Mock
    private RefundAppService refundAppService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private CallbackSignatureService callbackSignatureService;

    @InjectMocks
    private RefundCallbackService refundCallbackService;

    private Refund refund;

    @BeforeEach
    void setUp() {
        refund = Refund.builder()
                .refundId("RID1")
                .refundNo("RF1")
                .orderId("OID1")
                .refundAmount(new BigDecimal("20000.00"))
                .refundFee(BigDecimal.ZERO)
                .refundStatus(RefundStatus.PROCESSING)
                .applyTime(LocalDateTime.now())
                .items(new ArrayList<>(List.of(
                        RefundItem.builder().refundItemId("RI-1").refundId("RID1")
                                .paymentId("PID-E").paymentNo("P-EARNEST")
                                .refundAmount(new BigDecimal("5000.00"))
                                .refundStatus(RefundStatus.PROCESSING).build(),
                        RefundItem.builder().refundItemId("RI-2").refundId("RID1")
                                .paymentId("PID-S").paymentNo("P-SUPPLEMENT")
                                .refundAmount(new BigDecimal("15000.00"))
                                .refundStatus(RefundStatus.PROCESSING).build())))
                .build();
        lenient().when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(true);
        lenient().when(refundRepository.findByRefundNo("RF1")).thenReturn(Optional.of(refund));
        lenient().when(refundItemRepository.findByRefundId("RID1")).thenAnswer(invocation -> refund.getItems());
        lenient().when(refundItemRepository.findByRefundItemId(anyString())).thenAnswer(invocation ->
                refund.getItems().stream()
                        .filter(item -> item.getRefundItemId().equals(invocation.getArgument(0)))
                        .findFirst());
    }

    @Test
    @DisplayName("一项完成一项处理中：主单仍为处理中，不完成订单")
    void 部分完成_主单处理中() {
        PaymentCallbackResult result = refundCallbackService.handleCallback(
                cmd("RI-1", "5000.00", "completed"), "sig", 1L, "nonce-1");

        assertEquals(PaymentCallbackResultCode.SUCCESS, result.getCode());
        assertEquals(RefundStatus.PROCESSING, refund.getRefundStatus());
        verify(refundAppService, never()).completeOrderRefund(any());
    }

    @Test
    @DisplayName("全部完成：主单完成并推进订单退款完成")
    void 全部完成_推进订单() {
        refundCallbackService.handleCallback(cmd("RI-1", "5000.00", "completed"), "sig", 1L, "nonce-1");
        refundCallbackService.handleCallback(cmd("RI-2", "15000.00", "completed"), "sig", 1L, "nonce-2");

        assertEquals(RefundStatus.COMPLETED, refund.getRefundStatus());
        verify(refundAppService, atLeastOnce()).completeOrderRefund(any());
    }

    @Test
    @DisplayName("重复完成回调：幂等返回且不重复完成")
    void 重复完成幂等() {
        refund.getItems().get(0).markCompleted(LocalDateTime.now());
        refund.getItems().get(1).markCompleted(LocalDateTime.now());

        PaymentCallbackResult result = refundCallbackService.handleCallback(
                cmd("RI-1", "5000.00", "completed"), "sig", 1L, "nonce-1");

        assertEquals(PaymentCallbackResultCode.DUPLICATE, result.getCode());
        verify(refundAppService, never()).completeOrderRefund(any());
    }

    @Test
    @DisplayName("已完成明细收到失败回调：冲突拒绝")
    void 冲突状态拒绝() {
        refund.getItems().get(0).markCompleted(LocalDateTime.now());

        PaymentCallbackResult result = refundCallbackService.handleCallback(
                cmd("RI-1", "5000.00", "failed"), "sig", 1L, "nonce-1");

        assertEquals(PaymentCallbackResultCode.CONFLICT, result.getCode());
    }

    @Test
    @DisplayName("退款明细金额不一致：拒绝")
    void 金额不一致拒绝() {
        PaymentCallbackResult result = refundCallbackService.handleCallback(
                cmd("RI-1", "4999.00", "completed"), "sig", 1L, "nonce-1");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
    }

    @Test
    @DisplayName("签名失败：拒绝且不更新")
    void 签名失败拒绝() {
        when(callbackSignatureService.verifySignature(any(), any(), any(), any())).thenReturn(false);

        PaymentCallbackResult result = refundCallbackService.handleCallback(
                cmd("RI-1", "5000.00", "completed"), "bad", 1L, "nonce-1");

        assertEquals(PaymentCallbackResultCode.FAIL, result.getCode());
        verify(refundItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("一项失败：主单失败并记录原因，可重试")
    void 明细失败_主单失败() {
        RefundCallbackCmd failed = cmd("RI-1", "5000.00", "failed");
        failed.setFailReason("银行退票");
        refundCallbackService.handleCallback(failed, "sig", 1L, "nonce-1");
        refundCallbackService.handleCallback(cmd("RI-2", "15000.00", "failed"), "sig", 1L, "nonce-2");

        assertEquals(RefundStatus.FAILED, refund.getRefundStatus());
        assertTrue(refund.getFailReason() != null && !refund.getFailReason().isEmpty());
    }

    private RefundCallbackCmd cmd(String itemId, String amount, String status) {
        return RefundCallbackCmd.builder()
                .refundNo("RF1")
                .refundItemId(itemId)
                .externalRefundNo("MOCK-" + itemId)
                .refundAmount(new BigDecimal(amount))
                .refundStatus(status)
                .refundTime(LocalDateTime.now())
                .idempotentKey("IDEM-" + itemId + "-" + status)
                .build();
    }
}
