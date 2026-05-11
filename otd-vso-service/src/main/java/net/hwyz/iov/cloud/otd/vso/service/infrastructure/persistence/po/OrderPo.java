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
