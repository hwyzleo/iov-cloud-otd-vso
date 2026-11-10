package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.RefundAmountInvalidException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 退款主单领域对象
 * <p>
 * 主单金额等于全部明细金额之和；主单状态由明细聚合得出。
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    private String refundId;

    private String refundNo;

    private String orderId;

    private String refundScene;

    private BigDecimal refundAmount;

    private BigDecimal refundFee;

    private RefundStatus refundStatus;

    private String externalRefundNo;

    private LocalDateTime applyTime;

    private LocalDateTime refundTime;

    private String failReason;

    @Builder.Default
    private List<RefundItem> items = new ArrayList<>();

    /**
     * 校验主单金额与明细之和一致且大于 0
     */
    public void validateAmount() {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RefundAmountInvalidException("退款金额必须大于 0：" + refundAmount);
        }
        if (items != null && !items.isEmpty()) {
            BigDecimal itemTotal = items.stream()
                    .map(RefundItem::getRefundAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (itemTotal.compareTo(refundAmount) != 0) {
                throw new RefundAmountInvalidException(
                        "退款主单金额[" + refundAmount + "]与明细之和[" + itemTotal + "]不一致");
            }
        }
    }

    /**
     * 标记处理中
     */
    public void markProcessing() {
        if (refundStatus == RefundStatus.PENDING || refundStatus == RefundStatus.FAILED) {
            this.refundStatus = RefundStatus.PROCESSING;
            this.failReason = null;
        }
    }

    /**
     * 根据明细聚合主单状态：
     * 全部 completed → 主单 completed；
     * 任一项 failed 且无 pending/processing → 主单 failed；
     * 否则 processing。
     */
    public void aggregateStatus() {
        if (items == null || items.isEmpty()) {
            return;
        }
        boolean allCompleted = items.stream().allMatch(RefundItem::isCompleted);
        boolean anyFailed = items.stream().anyMatch(item -> item.getRefundStatus() == RefundStatus.FAILED);
        boolean anyActive = items.stream().anyMatch(item ->
                item.getRefundStatus() == RefundStatus.PENDING
                        || item.getRefundStatus() == RefundStatus.PROCESSING);

        if (allCompleted) {
            if (refundStatus != RefundStatus.COMPLETED) {
                this.refundStatus = RefundStatus.COMPLETED;
                this.refundTime = LocalDateTime.now();
            }
            this.failReason = null;
        } else if (anyFailed && !anyActive) {
            this.refundStatus = RefundStatus.FAILED;
            this.failReason = items.stream()
                    .filter(item -> item.getRefundStatus() == RefundStatus.FAILED)
                    .map(RefundItem::getFailReason)
                    .filter(reason -> reason != null && !reason.isEmpty())
                    .findFirst()
                    .orElse("退款明细处理失败");
        } else {
            this.refundStatus = RefundStatus.PROCESSING;
        }
    }

    /**
     * 是否已全部完成
     */
    public boolean isCompleted() {
        return refundStatus == RefundStatus.COMPLETED;
    }

    /**
     * 设置明细（用于仓储装配）
     */
    public void setItems(List<RefundItem> items) {
        this.items = items;
    }

    /**
     * 是否存在可重试明细
     */
    public boolean hasRetryableItem() {
        return items != null && items.stream().anyMatch(RefundItem::retryable);
    }
}
