package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单锁记录表持久化对象
 */
@Data
@TableName("vso_order_lock")
public class OrderLockPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_lock_id")
    private String orderLockId;

    @TableField("order_id")
    private String orderId;

    @TableField("lock_scene")
    private String lockScene;

    @TableField("lock_holder_id")
    private String lockHolderId;

    @TableField("lock_holder_role")
    private String lockHolderRole;

    @TableField("lock_start_time")
    private LocalDateTime lockStartTime;

    @TableField("lock_release_time")
    private LocalDateTime lockReleaseTime;

    @TableField("lock_release_method")
    private String lockReleaseMethod;

    @TableField("lock_release_reason")
    private String lockReleaseReason;

    @TableField("unlock_user_id")
    private String unlockUserId;

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
