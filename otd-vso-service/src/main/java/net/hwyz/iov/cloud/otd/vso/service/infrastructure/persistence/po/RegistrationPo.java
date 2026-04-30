package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 上牌跟踪表持久化对象
 */
@Data
@TableName("vso_registration")
public class RegistrationPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("registration_id")
    private String registrationId;

    @TableField("order_id")
    private String orderId;

    @TableField("registration_status")
    private String registrationStatus;

    @TableField("plate_no")
    private String plateNo;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("material_submit_time")
    private LocalDateTime materialSubmitTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("fail_reason")
    private String failReason;

    @TableField("external_apply_no")
    private String externalApplyNo;

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
