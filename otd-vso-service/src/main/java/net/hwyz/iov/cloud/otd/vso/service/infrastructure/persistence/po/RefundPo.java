package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款记录表持久化对象
 */
@Data
@TableName("vso_refund")
public class RefundPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("refund_id")
    private String refundId;

    @TableField("refund_no")
    private String refundNo;

    @TableField("order_id")
    private String orderId;

    @TableField("payment_id")
    private String paymentId;

    @TableField("refund_scene")
    private String refundScene;

    @TableField("refund_amount")
    private BigDecimal refundAmount;

    @TableField("refund_status")
    private String refundStatus;

    @TableField("approval_id")
    private String approvalId;

    @TableField("external_refund_no")
    private String externalRefundNo;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("refund_time")
    private LocalDateTime refundTime;

    @TableField("fail_reason")
    private String failReason;

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
