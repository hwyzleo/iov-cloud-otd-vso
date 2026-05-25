package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.ConfigChangeRefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;

import java.time.LocalDateTime;

/**
 * 改配退款领域对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangeRefund {

    private Long id;
    private String refundTaskNo;
    private String orderId;
    private Money refundAmount;
    private ConfigChangeRefundStatus refundStatus;
    private Integer configVersionNo;
    private String refundId;
    private String failReason;
    private String manualAuditStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 检查退款是否可以处理
     */
    public boolean canProcess() {
        return refundStatus == ConfigChangeRefundStatus.PENDING;
    }

    /**
     * 标记为处理中
     */
    public void processing() {
        this.refundStatus = ConfigChangeRefundStatus.PROCESSING;
    }

    /**
     * 标记为已完成
     */
    public void complete(String refundId) {
        this.refundStatus = ConfigChangeRefundStatus.COMPLETED;
        this.refundId = refundId;
    }

    /**
     * 标记为失败
     */
    public void fail(String reason) {
        this.refundStatus = ConfigChangeRefundStatus.FAILED;
        this.failReason = reason;
        this.manualAuditStatus = "pending";
    }

    /**
     * 检查是否需要人工审核
     */
    public boolean needManualAudit() {
        return refundStatus == ConfigChangeRefundStatus.FAILED && "pending".equals(manualAuditStatus);
    }
}
