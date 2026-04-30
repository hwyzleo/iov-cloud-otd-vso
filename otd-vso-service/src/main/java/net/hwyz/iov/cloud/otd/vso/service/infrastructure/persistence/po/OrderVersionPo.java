package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单版本表持久化对象
 */
@Data
@TableName("vso_order_version")
public class OrderVersionPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_version_id")
    private String orderVersionId;

    @TableField("order_id")
    private String orderId;

    @TableField("version_no")
    private Integer versionNo;

    @TableField("change_type")
    private String changeType;

    @TableField("change_reason")
    private String changeReason;

    @TableField("approval_id")
    private String approvalId;

    @TableField("trigger_source")
    private String triggerSource;

    @TableField("trigger_user_id")
    private String triggerUserId;

    @TableField("effective_flag")
    private Integer effectiveFlag;

    @TableField("effective_time")
    private LocalDateTime effectiveTime;

    @TableField("snapshot_json")
    private String snapshotJson;

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
