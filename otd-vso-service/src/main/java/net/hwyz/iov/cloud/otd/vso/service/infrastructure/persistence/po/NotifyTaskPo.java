package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知任务表持久化对象
 */
@Data
@TableName("vso_notify_task")
public class NotifyTaskPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("notify_task_id")
    private String notifyTaskId;

    @TableField("order_id")
    private String orderId;

    @TableField("notify_type")
    private String notifyType;

    @TableField("receiver_type")
    private String receiverType;

    @TableField("receiver_id")
    private String receiverId;

    @TableField("receiver_address")
    private String receiverAddress;

    @TableField("content_template_code")
    private String contentTemplateCode;

    @TableField("send_status")
    private String sendStatus;

    @TableField("retry_count")
    private Integer retryCount;

    @TableField("plan_send_time")
    private LocalDateTime planSendTime;

    @TableField("send_time")
    private LocalDateTime sendTime;

    @TableField("fail_reason")
    private String failReason;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    private Long modifyBy;

    @TableField(value = "row_version", fill = FieldFill.INSERT)
    private Integer rowVersion;

    @TableField(value = "row_valid", fill = FieldFill.INSERT)
    private Integer rowValid;
}
