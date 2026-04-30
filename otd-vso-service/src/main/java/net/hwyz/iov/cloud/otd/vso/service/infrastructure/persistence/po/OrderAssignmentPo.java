package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单归属与转派信息表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_assignment")
public class OrderAssignmentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 归属业务 ID
     */
    @TableField("assignment_id")
    private String assignmentId;

    /**
     * 订单业务 ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 下单门店编码
     */
    @TableField("order_store_code")
    private String orderStoreCode;

    /**
     * 归属门店编码
     */
    @TableField("owner_store_code")
    private String ownerStoreCode;

    /**
     * 交付门店编码
     */
    @TableField("delivery_store_code")
    private String deliveryStoreCode;

    /**
     * 原归属区域编码
     */
    @TableField("origin_region_code")
    private String originRegionCode;

    /**
     * 当前归属区域编码
     */
    @TableField("owner_region_code")
    private String ownerRegionCode;

    /**
     * 实际交付区域编码
     */
    @TableField("delivery_region_code")
    private String deliveryRegionCode;

    /**
     * 当前销售顾问编码
     */
    @TableField("sales_code")
    private String salesCode;

    /**
     * 归属动作
     */
    @TableField("assign_type")
    private String assignType;

    /**
     * 转派原因
     */
    @TableField("assign_reason")
    private String assignReason;

    /**
     * 归属生效时间
     */
    @TableField("assign_time")
    private LocalDateTime assignTime;

    /**
     * 操作人员编码
     */
    @TableField("operator_code")
    private String operatorCode;

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
