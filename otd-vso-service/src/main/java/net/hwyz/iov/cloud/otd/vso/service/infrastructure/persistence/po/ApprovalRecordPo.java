package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批流转记录表持久化对象
 */
@Data
@TableName("vso_approval_record")
public class ApprovalRecordPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("approval_record_id")
    private String approvalRecordId;

    @TableField("approval_id")
    private String approvalId;

    @TableField("node_no")
    private Integer nodeNo;

    @TableField("approve_type")
    private String approveType;

    @TableField("approver_id")
    private String approverId;

    @TableField("approver_role")
    private String approverRole;

    @TableField("approve_comment")
    private String approveComment;

    @TableField("attachment_url")
    private String attachmentUrl;

    @TableField("operate_time")
    private LocalDateTime operateTime;

    @TableField("timeout_flag")
    private Integer timeoutFlag;

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
