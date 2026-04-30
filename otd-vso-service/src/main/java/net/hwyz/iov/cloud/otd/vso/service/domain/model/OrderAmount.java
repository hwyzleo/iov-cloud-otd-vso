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
        this.unpaidTotal = new Money(unpaid.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : unpaid);
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

}
