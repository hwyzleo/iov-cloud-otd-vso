package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单维度状态表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_status_dimension")
public class OrderStatusDimensionPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 维度状态业务 ID
     */
    @TableField("status_dimension_id")
    private String statusDimensionId;

    /**
     * 订单业务 ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 审批状态
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 配车状态
     */
    @TableField("vehicle_status")
    private String vehicleStatus;

    /**
     * 合同状态
     */
    @TableField("contract_status")
    private String contractStatus;

    /**
     * 支付状态
     */
    @TableField("payment_status")
    private String paymentStatus;

    /**
     * 金融状态
     */
    @TableField("finance_status")
    private String financeStatus;

    /**
     * 补贴状态
     */
    @TableField("subsidy_status")
    private String subsidyStatus;

    /**
     * 交付预约/执行状态
     */
    @TableField("delivery_status")
    private String deliveryStatus;

    /**
     * 上牌状态
     */
    @TableField("registration_status")
    private String registrationStatus;

    /**
     * 发票状态
     */
    @TableField("invoice_status")
    private String invoiceStatus;

    /**
     * 资料状态
     */
    @TableField("material_status")
    private String materialStatus;

    /**
     * 异常状态
     */
    @TableField("exception_status")
    private String exceptionStatus;

    /**
     * 最近一次状态变更来源
     */
    @TableField("last_sync_source")
    private String lastSyncSource;

    /**
     * 最近一次状态变更时间
     */
    @TableField("last_sync_time")
    private LocalDateTime lastSyncTime;

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
