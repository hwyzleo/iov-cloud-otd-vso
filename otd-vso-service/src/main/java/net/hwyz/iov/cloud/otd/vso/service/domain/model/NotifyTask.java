package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知任务
 *
 * @author VSO Team
 */
@Getter
@lombok.Setter
@RequiredArgsConstructor
public class NotifyTask {

    /**
     * 通知业务 ID
     */
    private final String notifyId;

    /**
     * 订单业务 ID
     */
    private final String orderId;

    /**
     * 通知类型
     */
    private final String notifyType;

    /**
     * 接收人类型
     */
    private final String receiverType;

    /**
     * 接收人 ID
     */
    private final String receiverId;

    /**
     * 接收地址（手机号/邮箱/push token）
     */
    private String receiverAddress;

    /**
     * 模板编码
     */
    private String templateCode;

    /**
     * 模板参数
     */
    private java.util.Map<String, Object> templateParams;

    /**
     * 发送状态
     */
    private String sendStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 计划发送时间
     */
    private LocalDateTime planSendTime;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    public NotifyTask(String notifyId, String orderId, String notifyType, 
                     String receiverType, String receiverId) {
        this(notifyId, orderId, notifyType, receiverType, receiverId, LocalDateTime.now());
        this.sendStatus = "PENDING";
        this.retryCount = 0;
        this.planSendTime = LocalDateTime.now();
    }

    /**
     * 发送通知
     */
    public void send() {
        this.sendStatus = "SENT";
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 发送失败
     */
    public void fail(String reason) {
        this.sendStatus = "FAILED";
        this.failReason = reason;
        this.retryCount++;
    }

    /**
     * 是否可重试
     */
    public boolean canRetry() {
        return this.retryCount < 3 && "FAILED".equals(this.sendStatus);
    }

}
