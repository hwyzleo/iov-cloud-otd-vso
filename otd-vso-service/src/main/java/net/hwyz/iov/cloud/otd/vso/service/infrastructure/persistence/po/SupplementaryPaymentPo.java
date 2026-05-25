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
 * 改配补款记录表持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vso_supplementary_payment")
public class SupplementaryPaymentPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("supplementary_no")
    private String supplementaryNo;

    @TableField("order_id")
    private String orderId;

    @TableField("supplementary_amount")
    private BigDecimal supplementaryAmount;

    @TableField("currency")
    private String currency;

    @TableField("supplementary_status")
    private String supplementaryStatus;

    @TableField("config_version_no")
    private Integer configVersionNo;

    @TableField("payment_id")
    private String paymentId;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

}
