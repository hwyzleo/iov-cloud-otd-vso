package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * VIN占用有效期配置PO
 */
@Data
@TableName("vso_config_vehicle_occupancy")
public class VehicleOccupancyConfigPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("occupancy_rule_key")
    private String occupancyRuleKey;

    @TableField("model_code")
    private String modelCode;

    @TableField("order_type")
    private String orderType;

    @TableField("store_code")
    private String storeCode;

    @TableField("region_code")
    private String regionCode;

    @TableField("activity_type")
    private String activityType;

    @TableField("customer_type")
    private String customerType;

    @TableField("finance_order_flag")
    private Integer financeOrderFlag;

    @TableField("special_approval_flag")
    private Integer specialApprovalFlag;

    @TableField("occupancy_hours")
    private Integer occupancyHours;

    @TableField("enable_flag")
    private Integer enableFlag;

    @TableField("priority")
    private Integer priority;

    @TableField("remark")
    private String remark;

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