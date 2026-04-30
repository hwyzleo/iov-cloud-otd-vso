package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单金额口径表持久化对象
 *
 * @author VSO Team
 */
@Data
@TableName("vso_order_amount")
public class OrderAmountPo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 金额业务 ID
     */
    @TableField("amount_id")
    private String amountId;

    /**
     * 订单业务 ID
     */
    @TableField("order_id")
    private String orderId;

    /**
     * 指导价
     */
    @TableField("guide_price")
    private BigDecimal guidePrice;

    /**
     * 裸车价/车款
     */
    @TableField("vehicle_price")
    private BigDecimal vehiclePrice;

    /**
     * 选装价
     */
    @TableField("option_price")
    private BigDecimal optionPrice;

    /**
     * 颜色加价
     */
    @TableField("color_markup")
    private BigDecimal colorMarkup;

    /**
     * 服务费
     */
    @TableField("service_fee")
    private BigDecimal serviceFee;

    /**
     * 上牌服务费
     */
    @TableField("plate_service_fee")
    private BigDecimal plateServiceFee;

    /**
     * 保险费用
     */
    @TableField("insurance_fee")
    private BigDecimal insuranceFee;

    /**
     * 优惠合计
     */
    @TableField("discount_total")
    private BigDecimal discountTotal;

    /**
     * 补贴合计
     */
    @TableField("subsidy_total")
    private BigDecimal subsidyTotal;

    /**
     * 金融贴息合计
     */
    @TableField("finance_discount_total")
    private BigDecimal financeDiscountTotal;

    /**
     * 最终成交总价
     */
    @TableField("deal_price_total")
    private BigDecimal dealPriceTotal;

    /**
     * 定金金额
     */
    @TableField("deposit_amount")
    private BigDecimal depositAmount;

    /**
     * 首付款金额
     */
    @TableField("down_payment_amount")
    private BigDecimal downPaymentAmount;

    /**
     * 尾款金额
     */
    @TableField("tail_payment_amount")
    private BigDecimal tailPaymentAmount;

    /**
     * 已支付金额
     */
    @TableField("paid_total")
    private BigDecimal paidTotal;

    /**
     * 已退款金额
     */
    @TableField("refund_total")
    private BigDecimal refundTotal;

    /**
     * 订单应收金额
     */
    @TableField("receivable_total")
    private BigDecimal receivableTotal;

    /**
     * 净应收金额
     */
    @TableField("net_receivable_total")
    private BigDecimal netReceivableTotal;

    /**
     * 待支付金额
     */
    @TableField("unpaid_total")
    private BigDecimal unpaidTotal;

    /**
     * 开票金额
     */
    @TableField("invoice_amount")
    private BigDecimal invoiceAmount;

    /**
     * 金额计算版本
     */
    @TableField("calculation_version")
    private Integer calculationVersion;

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
