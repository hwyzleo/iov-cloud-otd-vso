package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交付预约表持久化对象
 */
@Data
@TableName("vso_delivery_appointment")
public class DeliveryAppointmentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("delivery_appointment_id")
    private String deliveryAppointmentId;

    @TableField("delivery_appointment_no")
    private String deliveryAppointmentNo;

    @TableField("order_id")
    private String orderId;

    @TableField("appointment_status")
    private String appointmentStatus;

    @TableField("delivery_mode")
    private String deliveryMode;

    @TableField("appointment_time")
    private LocalDateTime appointmentTime;

    @TableField("appointment_place")
    private String appointmentPlace;

    @TableField("appointment_store_code")
    private String appointmentStoreCode;

    @TableField("contact_name")
    private String contactName;

    @TableField("contact_mobile_encrypted")
    private String contactMobileEncrypted;

    @TableField("reschedule_reason")
    private String rescheduleReason;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("operator_id")
    private String operatorId;

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
