package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * 事务性事件发布记录（领域模型）
 * <p>
 * 与业务事实在同一本地事务写入；发布状态由中继推进。载荷为提交时刻的不可变快照。
 *
 * @author VSO Team
 */
@Getter
@Builder
public class OrderOutboxRecord {

    /**
     * 主键
     */
    private Long id;

    /**
     * 事件唯一 ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 聚合类型
     */
    private String aggregateType;

    /**
     * 聚合 ID
     */
    private String aggregateId;

    /**
     * 聚合版本
     */
    private Long aggregateVersion;

    /**
     * 注册的目标 Topic
     */
    private String topic;

    /**
     * Kafka Key
     */
    private String messageKey;

    /**
     * 不可变 JSON 载荷
     */
    private String payload;

    /**
     * 发布状态
     */
    private String publishState;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    private Date nextRetryTime;

    /**
     * Broker 确认时间
     */
    private Date publishedAt;

    /**
     * 脱敏的最后错误
     */
    private String lastError;
}
