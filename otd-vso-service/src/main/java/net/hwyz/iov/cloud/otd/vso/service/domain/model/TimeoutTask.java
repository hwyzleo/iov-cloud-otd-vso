package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 超时任务聚合根
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class TimeoutTask {

    /**
     * 任务业务 ID
     */
    private String taskId;

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String taskStatus;

    /**
     * 超时阈值（分钟）
     */
    private Integer thresholdMinutes;

    /**
     * 触发策略
     */
    private String triggerStrategy;

    /**
     * 计划触发时间
     */
    private LocalDateTime planTriggerTime;

    /**
     * 实际触发时间
     */
    private LocalDateTime actualTriggerTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public TimeoutTask(String taskId, String orderId, String taskType, 
                       String triggerStrategy, Integer thresholdMinutes) {
        this.taskId = taskId;
        this.orderId = orderId;
        this.taskType = taskType;
        this.taskStatus = "PENDING";
        this.triggerStrategy = triggerStrategy;
        this.thresholdMinutes = thresholdMinutes;
        this.retryCount = 0;
        this.createTime = LocalDateTime.now();
        this.planTriggerTime = createTime.plusMinutes(thresholdMinutes);
    }

    /**
     * 触发任务
     */
    public void trigger() {
        this.taskStatus = "TRIGGERED";
        this.actualTriggerTime = LocalDateTime.now();
    }

    /**
     * 标记为已完成
     */
    public void complete() {
        this.taskStatus = "DONE";
    }

    /**
     * 标记为失败
     */
    public void fail() {
        this.taskStatus = "FAILED";
        this.retryCount++;
    }

    /**
     * 标记为已取消
     */
    public void cancel() {
        this.taskStatus = "CANCELLED";
    }

    /**
     * 是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(planTriggerTime);
    }

    /**
     * 是否可重试
     */
    public boolean canRetry() {
        return this.retryCount < 3 && "FAILED".equals(this.taskStatus);
    }

}
