package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;

import java.math.BigDecimal;

/**
 * 订单金额聚合
 *
 * @author VSO Team
 */
@Getter
@NoArgsConstructor
public class OrderAmount {

    /**
     * 金额业务 ID
     */
    private String amountId;

    /**
     * 指导价
     */
    private Money guidePrice;

    /**
     * 裸车价/车款
     */
    private Money vehiclePrice;

    /**
     * 选装价
     */
    private Money optionPrice;

    /**
     * 颜色加价
     */
    private Money colorMarkup;

    /**
     * 服务费
     */
    private Money serviceFee;

    /**
     * 上牌服务费
     */
    private Money plateServiceFee;

    /**
     * 保险费用
     */
    private Money insuranceFee;

    /**
     * 优惠合计
     */
    private Money discountTotal;

    /**
     * 补贴合计
     */
    private Money subsidyTotal;

    /**
     * 金融贴息合计
     */
    private Money financeDiscountTotal;

    /**
     * 最终成交总价
     */
    private Money dealPriceTotal;

    /**
     * 定金金额
     */
    private Money depositAmount;

    /**
     * 首付款金额
     */
    private Money downPaymentAmount;

    /**
     * 尾款金额
     */
    private Money tailPaymentAmount;

    /**
     * 已支付金额
     */
    private Money paidTotal;

    /**
     * 已退款金额
     */
    private Money refundTotal;

    /**
     * 订单应收金额
     */
    private Money receivableTotal;

    /**
     * 净应收金额
     */
    private Money netReceivableTotal;

    /**
     * 待支付金额
     */
    private Money unpaidTotal;

    /**
     * 开票金额
     */
    private Money invoiceAmount;

    /**
     * 金额计算版本
     */
    private Integer calculationVersion;

    public OrderAmount(String amountId) {
        this.amountId = amountId;
        this.guidePrice = new Money(BigDecimal.ZERO);
        this.vehiclePrice = new Money(BigDecimal.ZERO);
        this.optionPrice = new Money(BigDecimal.ZERO);
        this.colorMarkup = new Money(BigDecimal.ZERO);
        this.serviceFee = new Money(BigDecimal.ZERO);
        this.plateServiceFee = new Money(BigDecimal.ZERO);
        this.insuranceFee = new Money(BigDecimal.ZERO);
        this.discountTotal = new Money(BigDecimal.ZERO);
        this.subsidyTotal = new Money(BigDecimal.ZERO);
        this.financeDiscountTotal = new Money(BigDecimal.ZERO);
        this.dealPriceTotal = new Money(BigDecimal.ZERO);
        this.depositAmount = new Money(BigDecimal.ZERO);
        this.downPaymentAmount = new Money(BigDecimal.ZERO);
        this.tailPaymentAmount = new Money(BigDecimal.ZERO);
        this.paidTotal = new Money(BigDecimal.ZERO);
        this.refundTotal = new Money(BigDecimal.ZERO);
        this.receivableTotal = new Money(BigDecimal.ZERO);
        this.netReceivableTotal = new Money(BigDecimal.ZERO);
        this.unpaidTotal = new Money(BigDecimal.ZERO);
        this.invoiceAmount = new Money(BigDecimal.ZERO);
        this.calculationVersion = 1;
    }

    /**
     * 计算成交总价
     */
    public void calculateDealPrice() {
        BigDecimal total = vehiclePrice.getAmount()
                .add(optionPrice.getAmount())
                .add(colorMarkup.getAmount())
                .add(serviceFee.getAmount())
                .add(plateServiceFee.getAmount())
                .add(insuranceFee.getAmount())
                .subtract(discountTotal.getAmount())
                .subtract(subsidyTotal.getAmount())
                .subtract(financeDiscountTotal.getAmount());
        this.dealPriceTotal = new Money(total);
    }

    /**
     * 计算净应收金额
     */
    public void calculateNetReceivable() {
        BigDecimal total = dealPriceTotal.getAmount();
        this.netReceivableTotal = new Money(total);
    }

    /**
     * 计算待支付金额
     */
    public void calculateUnpaid() {
        BigDecimal unpaid = netReceivableTotal.getAmount()
                .subtract(paidTotal.getAmount())
                .add(refundTotal.getAmount());
        this.unpaidTotal = new Money(unpaid.max(BigDecimal.ZERO));
    }

    /**
     * 是否已结清
     */
    public boolean isPaidOff() {
        return unpaidTotal.isZero() || unpaidTotal.isLessThan(new Money(BigDecimal.valueOf(0.01)));
    }

    /**
     * 重算所有金额
     */
    public void recalculate() {
        calculateDealPrice();
        calculateNetReceivable();
        calculateUnpaid();
        this.calculationVersion++;
    }

    /**
     * 设置已支付金额
     */
    public void setPaidTotal(Money paidTotal) {
        this.paidTotal = paidTotal;
        calculateUnpaid();
    }

    /**
     * 计算退款金额
     *
     * @param orderState 订单状态
     * @return 退款金额（已支付金额 - 手续费）
     * @throws IllegalStateException 如果订单状态不允许退款
     */
    public Money calculateRefundAmount(OrderState orderState) {
        // 根据订单状态判断退款规则
        switch (orderState) {
            case EARNEST_MONEY_PAID:
            case DOWN_PAYMENT_PAID:
                // 未锁单前：全额退款，手续费为 0
                return this.paidTotal;
                
            case ARRANGE_PRODUCTION:
                // 锁单后：部分退款，扣除手续费
                Money fee = calculateRefundFee(this.paidTotal);
                return this.paidTotal.subtract(fee);
                
            default:
                // 生产中/已发运：不支持退款
                throw new IllegalStateException("订单状态 [" + orderState + "] 不支持退款");
        }
    }

    /**
     * 计算退款手续费
     * 手续费 = max(已支付金额 × 5%, 500)
     *
     * @param paidAmount 已支付金额
     * @return 手续费
     */
    private Money calculateRefundFee(Money paidAmount) {
        BigDecimal percentageFee = paidAmount.getAmount().multiply(new BigDecimal("0.05"));
        BigDecimal minFee = new BigDecimal("500.00");
        BigDecimal fee = percentageFee.max(minFee);
        return new Money(fee);
    }

    /**
     * 检查是否可以退款
     *
     * @param orderState 订单状态
     * @return 是否可以退款
     */
    public boolean canRefund(OrderState orderState) {
        return orderState == OrderState.EARNEST_MONEY_PAID 
            || orderState == OrderState.DOWN_PAYMENT_PAID 
            || orderState == OrderState.ARRANGE_PRODUCTION;
    }

    /**
     * 获取退款场景
     *
     * @param orderState 订单状态
     * @return 退款场景（full_refund 或 partial_refund）
     */
    public String getRefundScene(OrderState orderState) {
        switch (orderState) {
            case EARNEST_MONEY_PAID:
            case DOWN_PAYMENT_PAID:
                return "full_refund";
            case ARRANGE_PRODUCTION:
                return "partial_refund";
            default:
                throw new IllegalStateException("订单状态 [" + orderState + "] 不支持退款");
        }
    }

    /**
     * 计算价格差额
     * @param newVehiclePrice 新配置的车辆价格
     * @param newOptionPrice 新配置的选装价格
     * @return 价格差额（正数表示需要补款，负数表示需要退款）
     */
    public Money calculatePriceDifference(Money newVehiclePrice, Money newOptionPrice) {
        Money currentTotal = this.vehiclePrice.add(this.optionPrice);
        Money newTotal = newVehiclePrice.add(newOptionPrice);
        return newTotal.subtract(currentTotal);
    }

    /**
     * 设置车辆价格
     */
    public void setVehiclePrice(Money vehiclePrice) {
        this.vehiclePrice = vehiclePrice;
    }

    /**
     * 设置选装价格
     */
    public void setOptionPrice(Money optionPrice) {
        this.optionPrice = optionPrice;
    }

    /**
     * 计算意向金转定金的差额
     * 差额 = 定金金额 - 意向金金额（depositAmount）
     *
     * @return 差额金额（正数表示需要补款，负数或零表示无需补款）
     */
    public Money calculateEarnestToDownDifference() {
        return this.downPaymentAmount.subtract(this.depositAmount);
    }

    /**
     * 更新已支付金额（差额支付成功后调用）
     *
     * @param additionalAmount 新增支付金额
     */
    public void addPaidAmount(Money additionalAmount) {
        this.paidTotal = this.paidTotal.add(additionalAmount);
        calculateUnpaid();
    }

    /**
     * 设置定金金额
     */
    public void setDepositAmount(Money depositAmount) {
        this.depositAmount = depositAmount;
    }

    /**
     * 设置首付款金额
     */
    public void setDownPaymentAmount(Money downPaymentAmount) {
        this.downPaymentAmount = downPaymentAmount;
    }

}
