package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批单表持久化对象
 */
@Data
@TableName("vso_approval")
public class ApprovalPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("approval_id")
    private String approvalId;

    @TableField("approval_no")
    private String approvalNo;

    @TableField("order_id")
    private String orderId;

    @TableField("approval_type")
    private String approvalType;

    @TableField("approval_status")
    private String approvalStatus;

    @TableField("apply_user_id")
    private String applyUserId;

    @TableField("apply_role")
    private String applyRole;

    @TableField("apply_reason")
    private String applyReason;

    @TableField("change_snapshot")
    private String changeSnapshot;

    @TableField("current_node_no")
    private Integer currentNodeNo;

    @TableField("executor_result")
    private String executorResult;

    @TableField("submit_time")
    private LocalDateTime submitTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

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
