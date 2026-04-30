package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 交付完成记录表持久化对象
 */
@Data
@TableName("vso_delivery_record")
public class DeliveryRecordPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("delivery_record_id")
    private String deliveryRecordId;

    @TableField("order_id")
    private String orderId;

    @TableField("delivery_appointment_id")
    private String deliveryAppointmentId;

    @TableField("delivery_status")
    private String deliveryStatus;

    @TableField("delivery_mode")
    private String deliveryMode;

    @TableField("actual_delivery_time")
    private LocalDateTime actualDeliveryTime;

    @TableField("delivery_place")
    private String deliveryPlace;

    @TableField("delivery_store_code")
    private String deliveryStoreCode;

    @TableField("receiver_name")
    private String receiverName;

    @TableField("receiver_mobile_encrypted")
    private String receiverMobileEncrypted;

    @TableField("receiver_is_buyer")
    private Integer receiverIsBuyer;

    @TableField("authorized_flag")
    private Integer authorizedFlag;

    @TableField("authorized_proof_type")
    private String authorizedProofType;

    @TableField("vehicle_vin")
    private String vehicleVin;

    @TableField("vehicle_mileage")
    private Integer vehicleMileage;

    @TableField("delivery_remark")
    private String deliveryRemark;

    @TableField("exception_remark")
    private String exceptionRemark;

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
