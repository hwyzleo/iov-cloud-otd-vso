package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物理删除审计影子记录表持久化对象
 */
@Data
@TableName("vso_order_shadow_delete")
public class OrderShadowDeletePo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("shadow_delete_id")
    private String shadowDeleteId;

    @TableField("origin_order_no")
    private String originOrderNo;

    @TableField("origin_small_order_no")
    private String originSmallOrderNo;

    @TableField("delete_approval_id")
    private String deleteApprovalId;

    @TableField("delete_reason")
    private String deleteReason;

    @TableField("before_main_status")
    private String beforeMainStatus;

    @TableField("compliance_delete_flag")
    private Integer complianceDeleteFlag;

    @TableField("delete_user_id")
    private String deleteUserId;

    @TableField("delete_time")
    private LocalDateTime deleteTime;

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
