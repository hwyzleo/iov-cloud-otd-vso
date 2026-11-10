package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款原支付明细领域对象
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundItem {

    private String refundItemId;

    private String refundId;

    private String paymentId;

    private String paymentNo;

    private BigDecimal refundAmount;

    private RefundStatus refundStatus;

    private String externalRefundNo;

    private String failReason;

    private LocalDateTime refundTime;

    /**
     * 绑定退款主单（主单 ID 生成后回填）
     */
    public void assignRefund(String refundId) {
        this.refundId = refundId;
    }

    /**
     * 标记处理中
     */
    public void markProcessing(String externalRefundNo) {
        if (refundStatus == null || refundStatus == RefundStatus.PENDING || refundStatus == RefundStatus.FAILED) {
            this.refundStatus = RefundStatus.PROCESSING;
            if (externalRefundNo != null) {
                this.externalRefundNo = externalRefundNo;
            }
            this.failReason = null;
        }
    }

    /**
     * 标记完成；重复完成幂等
     */
    public void markCompleted(LocalDateTime refundTime) {
        if (refundStatus == RefundStatus.COMPLETED) {
            return;
        }
        this.refundStatus = RefundStatus.COMPLETED;
        this.refundTime = refundTime != null ? refundTime : LocalDateTime.now();
        this.failReason = null;
    }

    /**
     * 标记失败
     */
    public void markFailed(String failReason, LocalDateTime refundTime) {
        if (refundStatus == RefundStatus.COMPLETED) {
            return;
        }
        this.refundStatus = RefundStatus.FAILED;
        this.failReason = failReason;
        this.refundTime = refundTime;
    }

    /**
     * 是否可重试（失败或中断遗留的 pending）
     */
    public boolean retryable() {
        return refundStatus == RefundStatus.FAILED || refundStatus == RefundStatus.PENDING;
    }

    public boolean isCompleted() {
        return refundStatus == RefundStatus.COMPLETED;
    }
}
