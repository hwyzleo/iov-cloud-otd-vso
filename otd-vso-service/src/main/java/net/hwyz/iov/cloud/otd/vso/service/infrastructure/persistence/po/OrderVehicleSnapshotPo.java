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

    @TableField("build_config_code")
    private String buildConfigCode;

    @TableField("build_config_name")
    private String buildConfigName;

    @TableField("feature_config_snapshot")
    private String featureConfigSnapshot;

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
     * Variant 编码
     */
    @TableField("variant_code")
    private String variantCode;

    /**
     * Configuration 编码
     */
    @TableField("configuration_code")
    private String configurationCode;

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
}