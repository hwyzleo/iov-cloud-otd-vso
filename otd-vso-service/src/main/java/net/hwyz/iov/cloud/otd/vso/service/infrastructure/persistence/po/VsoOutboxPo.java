package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.util.Date;

/**
 * <p>
 * VSO 事务性事件发布记录 数据对象
 * </p>
 *
 * @author VSO Team
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vso_outbox")
public class VsoOutboxPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 事件唯一 ID
     */
    @TableField("event_id")
    private String eventId;

    /**
     * 事件类型
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 聚合类型
     */
    @TableField("aggregate_type")
    private String aggregateType;

    /**
     * 聚合 ID
     */
    @TableField("aggregate_id")
    private String aggregateId;

    /**
     * 聚合版本
     */
    @TableField("aggregate_version")
    private Long aggregateVersion;

    /**
     * 注册的目标 Topic
     */
    @TableField("topic")
    private String topic;

    /**
     * Kafka Key
     */
    @TableField("message_key")
    private String messageKey;

    /**
     * 不可变 JSON 载荷
     */
    @TableField("payload")
    private String payload;

    /**
     * 发布状态
     */
    @TableField("publish_state")
    private String publishState;

    /**
     * 已重试次数
     */
    @TableField("retry_count")
    private Integer retryCount;

    /**
     * 下次重试时间
     */
    @TableField("next_retry_time")
    private Date nextRetryTime;

    /**
     * Broker 确认时间
     */
    @TableField("published_at")
    private Date publishedAt;

    /**
     * 脱敏的最后错误
     */
    @TableField("last_error")
    private String lastError;
}
