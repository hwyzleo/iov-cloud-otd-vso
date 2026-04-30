package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单业务时间线表持久化对象
 */
@Data
@TableName("vso_order_timeline")
public class OrderTimelinePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("timeline_id")
    private String timelineId;

    @TableField("order_id")
    private String orderId;

    @TableField("event_type")
    private String eventType;

    @TableField("event_name")
    private String eventName;

    @TableField("before_status")
    private String beforeStatus;

    @TableField("after_status")
    private String afterStatus;

    @TableField("operator_id")
    private String operatorId;

    @TableField("operator_role")
    private String operatorRole;

    @TableField("operate_source")
    private String operateSource;

    @TableField("related_doc_no")
    private String relatedDocNo;

    @TableField("external_system_name")
    private String externalSystemName;

    @TableField("result")
    private String result;

    @TableField("fail_reason")
    private String failReason;

    @TableField("event_remark")
    private String eventRemark;

    @TableField("event_time")
    private LocalDateTime eventTime;

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
