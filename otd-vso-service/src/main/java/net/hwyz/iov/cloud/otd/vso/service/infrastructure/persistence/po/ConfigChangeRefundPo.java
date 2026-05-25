package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 改配退款记录表持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vso_config_change_refund")
public class ConfigChangeRefundPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("refund_task_no")
    private String refundTaskNo;

    @TableField("order_id")
    private String orderId;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("currency")
    private String currency;

    @TableField("refund_status")
    private String refundStatus;

    @TableField("config_version_no")
    private Integer configVersionNo;

    @TableField("refund_id")
    private String refundId;

    @TableField("fail_reason")
    private String failReason;

    @TableField("manual_audit_status")
    private String manualAuditStatus;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

}
