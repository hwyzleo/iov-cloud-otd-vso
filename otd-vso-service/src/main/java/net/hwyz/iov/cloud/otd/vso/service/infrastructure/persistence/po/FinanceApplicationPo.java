package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 金融申请表持久化对象
 */
@Data
@TableName("vso_finance_application")
public class FinanceApplicationPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("finance_application_id")
    private String financeApplicationId;

    @TableField("order_id")
    private String orderId;

    @TableField("finance_type")
    private String financeType;

    @TableField("finance_provider")
    private String financeProvider;

    @TableField("finance_status")
    private String financeStatus;

    @TableField("loan_amount")
    private BigDecimal loanAmount;

    @TableField("interest_discount_amount")
    private BigDecimal interestDiscountAmount;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("approve_time")
    private LocalDateTime approveTime;

    @TableField("disbursement_time")
    private LocalDateTime disbursementTime;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("external_apply_no")
    private String externalApplyNo;

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
