package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.Data;

/**
 * 管理后台订单金额快照。
 *
 * @author VSO Team
 */
@Data
public class OrderAmountMpt {

    private String amountId;
    private MoneyMpt guidePrice;
    private MoneyMpt vehiclePrice;
    private MoneyMpt optionPrice;
    private MoneyMpt colorMarkup;
    private MoneyMpt serviceFee;
    private MoneyMpt plateServiceFee;
    private MoneyMpt insuranceFee;
    private MoneyMpt discountTotal;
    private MoneyMpt subsidyTotal;
    private MoneyMpt financeDiscountTotal;
    private MoneyMpt dealPriceTotal;
    private MoneyMpt depositAmount;
    private MoneyMpt downPaymentAmount;
    private MoneyMpt tailPaymentAmount;
    private MoneyMpt paidTotal;
    private MoneyMpt refundTotal;
    private MoneyMpt receivableTotal;
    private MoneyMpt netReceivableTotal;
    private MoneyMpt unpaidTotal;
    private MoneyMpt invoiceAmount;
    private Integer calculationVersion;
    private boolean upgradedFromSmall;
}
