package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配车与车辆绑定表持久化对象
 */
@Data
@TableName("vso_vehicle_assignment")
public class VehicleAssignmentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("vehicle_assignment_id")
    private String vehicleAssignmentId;

    @TableField("order_id")
    private String orderId;

    @TableField("assignment_type")
    private String assignmentType;

    @TableField("vehicle_source_type")
    private String vehicleSourceType;

    @TableField("vin")
    private String vin;

    @TableField("vehicle_id")
    private String vehicleId;

    @TableField("assign_status")
    private String assignStatus;

    @TableField("manual_assign_flag")
    private Integer manualAssignFlag;

    @TableField("manual_assign_reason")
    private String manualAssignReason;

    @TableField("unbind_reason")
    private String unbindReason;

    @TableField("occupy_expire_time")
    private LocalDateTime occupyExpireTime;

    @TableField("assign_time")
    private LocalDateTime assignTime;

    @TableField("bind_time")
    private LocalDateTime bindTime;

    @TableField("release_time")
    private LocalDateTime releaseTime;

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
