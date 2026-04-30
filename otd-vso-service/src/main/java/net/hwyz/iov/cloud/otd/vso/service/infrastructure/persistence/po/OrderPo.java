package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;

/**
 * 订单主表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order")
public class OrderPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("small_order_no")
    private String smallOrderNo;

    @TableField("order_type")
    private String orderType;

    @TableField("order_source")
    private String orderSource;

    @TableField("source_remark")
    private String sourceRemark;

    @TableField("customer_type")
    private String customerType;

    @TableField("main_status")
    private String mainStatus;

    @TableField("end_type")
    private String endType;

    @TableField("previous_main_status")
    private String previousMainStatus;

    @TableField("brand_code")
    private String brandCode;

    @TableField("region_code")
    private String regionCode;

    @TableField("store_code")
    private String storeCode;

    @TableField("sales_code")
    private String salesCode;

    @TableField("vehicle_vin")
    private String vehicleVin;

    @TableField("has_exception")
    private Integer hasException;

    @TableField("current_version_no")
    private Integer currentVersionNo;

    @TableField("locked_flag")
    private Integer lockedFlag;

    @TableField("reopen_flag")
    private Integer reopenFlag;

    @TableField("cancel_reason")
    private String cancelReason;

    @TableField("close_reason")
    private String closeReason;

    @TableField("void_reason")
    private String voidReason;

    @TableField("created_at_business")
    private LocalDateTime createdAtBusiness;

    @TableField("audit_submit_time")
    private LocalDateTime auditSubmitTime;

    @TableField("audit_pass_time")
    private LocalDateTime auditPassTime;

    @TableField("lock_time")
    private LocalDateTime lockTime;

    @TableField("delivery_finish_time")
    private LocalDateTime deliveryFinishTime;

    @TableField("finish_time")
    private LocalDateTime finishTime;

    @TableField("cancel_time")
    private LocalDateTime cancelTime;

    @TableField("close_time")
    private LocalDateTime closeTime;

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

    @TableField("order_state")
    private Integer orderState;

    @TableField("order_time")
    private Date orderTime;

    @TableField("order_state_time")
    private Date orderStateTime;

    @TableField("sale_code")
    private String saleCode;

    @TableField("build_config_code")
    private String buildConfigCode;

    @TableField("model_config_type")
    private String modelConfigType;

    @TableField("model_config_name")
    private String modelConfigName;

    @TableField("model_config_price")
    private java.math.BigDecimal modelConfigPrice;

    @TableField("model_config_map")
    private String modelConfigMap;

    @TableField("model_config_desc")
    private String modelConfigDesc;

    @TableField("total_price")
    private java.math.BigDecimal totalPrice;

    @TableField("license_city")
    private String licenseCity;

    @TableField("dealership")
    private String dealership;

    @TableField("delivery_center")
    private String deliveryCenter;

    @TableField("delivery_vin")
    private String deliveryVin;

    @TableField("order_person_id")
    private String orderPersonId;

    @TableField("order_person_type")
    private Integer orderPersonType;

    @TableField("order_person_name")
    private String orderPersonName;

    @TableField("order_person_id_type")
    private Integer orderPersonIdType;

    @TableField("order_person_id_num")
    private String orderPersonIdNum;

    @TableField("purchase_plan")
    private Integer purchasePlan;

    @TableField("transport_apply_person_id")
    private String transportApplyPersonId;

    @TableField("transport_apply_person_name")
    private String transportApplyPersonName;

    @TableField("delivery_person_id")
    private String deliveryPersonId;

    @TableField("delivery_person_name")
    private String deliveryPersonName;

}
