package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("vso_order_vehicle_snapshot")
public class OrderVehicleSnapshotPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("snapshot_id")
    private String snapshotId;

    @TableField("order_id")
    private String orderId;

    @TableField("sale_model_code")
    private String saleModelCode;

    @TableField("sale_model_name")
    private String saleModelName;

    @TableField("configuration_code")
    private String configurationCode;

    @TableField("configuration_name")
    private String configurationName;

    @TableField("snapshot_version")
    private Integer snapshotVersion;

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

    /**
     * Carline 编码
     */
    @TableField("carline_code")
    private String carlineCode;

    /**
     * Model 编码
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * Variant 编码
     */
    @TableField("variant_code")
    private String variantCode;

    /**
     * OptionCode 列表（JSON）
     */
    @TableField("option_codes")
    private String optionCodes;

    /**
     * Option 价格明细（JSON）
     */
    @TableField("option_price_breakdown")
    private String optionPriceBreakdown;

    /**
     * 销售策略快照（JSON）
     */
    @TableField("sale_policy_snapshot")
    private String salePolicySnapshot;

    /**
     * Model 销售策略快照（JSON）
     */
    @TableField("model_policy_snapshot")
    private String modelPolicySnapshot;

    /**
     * Variant 销售策略快照（JSON）
     */
    @TableField("variant_policy_snapshot")
    private String variantPolicySnapshot;

    /**
     * Configuration 销售白名单快照（JSON）
     */
    @TableField("config_policy_snapshot")
    private String configPolicySnapshot;
}