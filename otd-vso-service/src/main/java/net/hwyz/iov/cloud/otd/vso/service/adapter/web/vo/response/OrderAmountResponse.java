package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.response;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单金额信息响应对象
 *
 * @author VSO Team
 */
@Data
public class OrderAmountResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 金额业务 ID
     */
    private String amountId;

    /**
     * 指导价
     */
    private BigDecimal guidePrice;

    /**
     * 裸车价/车款
     */
    private BigDecimal vehiclePrice;

    /**
     * 选装价
     */
    private BigDecimal optionPrice;

    /**
     * 颜色加价
     */
    private BigDecimal colorMarkup;

    /**
     * 服务费
     */
    private BigDecimal serviceFee;

    /**
     * 上牌服务费
     */
    private BigDecimal plateServiceFee;

    /**
     * 保险费用
     */
    private BigDecimal insuranceFee;

    /**
     * 优惠合计
     */
    private BigDecimal discountTotal;

    /**
     * 补贴合计
     */
    private BigDecimal subsidyTotal;

    /**
     * 金融贴息合计
     */
    private BigDecimal financeDiscountTotal;

    /**
     * 最终成交总价
     */
    private BigDecimal dealPriceTotal;

    /**
     * 定金金额
     */
    private BigDecimal depositAmount;

    /**
     * 首付款金额
     */
    private BigDecimal downPaymentAmount;

    /**
     * 尾款金额
     */
    private BigDecimal tailPaymentAmount;

    /**
     * 已支付金额
     */
    private BigDecimal paidTotal;

    /**
     * 已退款金额
     */
    private BigDecimal refundTotal;

    /**
     * 订单应收金额
     */
    private BigDecimal receivableTotal;

    /**
     * 净应收金额
     */
    private BigDecimal netReceivableTotal;

    /**
     * 待支付金额
     */
    private BigDecimal unpaidTotal;

    /**
     * 开票金额
     */
    private BigDecimal invoiceAmount;

}
