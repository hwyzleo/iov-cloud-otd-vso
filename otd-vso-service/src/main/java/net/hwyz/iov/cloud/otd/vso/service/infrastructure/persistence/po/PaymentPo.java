package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录表持久化对象
 */
@Data
@TableName("vso_payment")
public class PaymentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("payment_id")
    private String paymentId;

    @TableField("payment_no")
    private String paymentNo;

    @TableField("order_id")
    private String orderId;

    @TableField("payment_stage")
    private String paymentStage;

    @TableField("payment_channel")
    private String paymentChannel;

    @TableField("payment_amount")
    private BigDecimal paymentAmount;

    @TableField("payment_status")
    private String paymentStatus;

    @TableField("initiator_role")
    private String initiatorRole;

    @TableField("initiator_id")
    private String initiatorId;

    @TableField("authorized_flag")
    private Integer authorizedFlag;

    @TableField("authorized_proof_type")
    private String authorizedProofType;

    @TableField("external_trade_no")
    private String externalTradeNo;

    @TableField("pay_time")
    private LocalDateTime payTime;

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
