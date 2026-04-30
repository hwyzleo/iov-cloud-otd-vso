package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单车型配置快照表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_vehicle_snapshot")
public class OrderVehicleSnapshotPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 快照业务 ID
     */
    @TableField("snapshot_id")
    private String snapshotId;

    /**
     * 订单业务 ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 车型编码
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * 车型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 配置编码
     */
    @TableField("config_code")
    private String configCode;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 颜色编码
     */
    @TableField("color_code")
    private String colorCode;

    /**
     * 颜色名称
     */
    @TableField("color_name")
    private String colorName;

    /**
     * 选装项快照
     */
    @TableField("option_snapshot")
    private String optionSnapshot;

    /**
     * 可售口径编码
     */
    @TableField("sale_scope_code")
    private String saleScopeCode;

    /**
     * 展示文案快照
     */
    @TableField("display_snapshot")
    private String displaySnapshot;

    /**
     * 快照版本号
     */
    @TableField("snapshot_version")
    private Integer snapshotVersion;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 创建者
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 修改时间
     */
    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    /**
     * 修改者
     */
    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    private Long modifyBy;

    /**
     * 记录版本
     */
    @TableField(value = "row_version", fill = FieldFill.INSERT)
    private Integer rowVersion;

    /**
     * 是否有效
     */
    @TableField(value = "row_valid", fill = FieldFill.INSERT)
    private Integer rowValid;

}
