package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vso_order")
public class OrderPo extends BasePo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private String orderId;

    @TableField("order_no")
    private String orderNo;

    @TableField("order_type")
    private String orderType;

    @TableField("order_source")
    private String orderSource;

    @TableField("source_remark")
    private String sourceRemark;

    @TableField("customer_type")
    private String customerType;

    @TableField("payment_method")
    private String paymentMethod;

    @TableField("license_city")
    private String licenseCity;

    @TableField("order_state")
    private Integer orderState;

    @TableField("end_type")
    private String endType;

    @TableField("previous_order_state")
    private Integer previousOrderState;

    @TableField("brand_code")
    private String brandCode;

    @TableField("order_store_code")
    private String orderStoreCode;

    @TableField("owner_store_code")
    private String ownerStoreCode;

    @TableField("owner_region_code")
    private String ownerRegionCode;

    @TableField("delivery_store_code")
    private String deliveryStoreCode;

    @TableField("delivery_region_code")
    private String deliveryRegionCode;

    @TableField("sales_code")
    private String salesCode;

    @TableField("sale_model")
    private String saleModel;

    @TableField("vehicle_vin")
    private String vehicleVin;

    @TableField("build_config_code")
    private String buildConfigCode;

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

}
