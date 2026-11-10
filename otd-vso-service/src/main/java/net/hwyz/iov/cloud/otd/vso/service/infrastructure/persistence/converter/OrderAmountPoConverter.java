package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;

/**
 * 订单金额对象转换器（Domain ↔ PO）
 *
 * @author VSO Team
 */
public final class OrderAmountPoConverter {

    private OrderAmountPoConverter() {
    }

    public static OrderAmount toDomain(OrderAmountPo po) {
        if (po == null) {
            return null;
        }
        OrderAmount amount = new OrderAmount(po.getAmountId());
        amount.setGuidePrice(money(po.getGuidePrice()));
        amount.setVehiclePrice(money(po.getVehiclePrice()));
        amount.setOptionPrice(money(po.getOptionPrice()));
        amount.setColorMarkup(money(po.getColorMarkup()));
        amount.setServiceFee(money(po.getServiceFee()));
        amount.setPlateServiceFee(money(po.getPlateServiceFee()));
        amount.setInsuranceFee(money(po.getInsuranceFee()));
        amount.setDiscountTotal(money(po.getDiscountTotal()));
        amount.setSubsidyTotal(money(po.getSubsidyTotal()));
        amount.setFinanceDiscountTotal(money(po.getFinanceDiscountTotal()));
        amount.setDealPriceTotal(money(po.getDealPriceTotal()));
        amount.setDepositAmount(money(po.getDepositAmount()));
        amount.setDownPaymentAmount(money(po.getDownPaymentAmount()));
        amount.setTailPaymentAmount(money(po.getTailPaymentAmount()));
        amount.setPaidTotal(money(po.getPaidTotal()));
        amount.setRefundTotal(money(po.getRefundTotal()));
        amount.setReceivableTotal(money(po.getReceivableTotal()));
        amount.setNetReceivableTotal(money(po.getNetReceivableTotal()));
        amount.setUnpaidTotal(money(po.getUnpaidTotal()));
        amount.setInvoiceAmount(money(po.getInvoiceAmount()));
        amount.setCalculationVersion(po.getCalculationVersion());
        amount.setUpgradedFromSmall(po.getUpgradedFromSmall() != null && po.getUpgradedFromSmall() == 1);
        return amount;
    }

    public static OrderAmountPo fromDomain(OrderAmount amount, String orderId, Long id) {
        OrderAmountPo po = new OrderAmountPo();
        po.setId(id);
        po.setAmountId(amount.getAmountId());
        po.setOrderId(orderId);
        po.setGuidePrice(value(amount.getGuidePrice()));
        po.setVehiclePrice(value(amount.getVehiclePrice()));
        po.setOptionPrice(value(amount.getOptionPrice()));
        po.setColorMarkup(value(amount.getColorMarkup()));
        po.setServiceFee(value(amount.getServiceFee()));
        po.setPlateServiceFee(value(amount.getPlateServiceFee()));
        po.setInsuranceFee(value(amount.getInsuranceFee()));
        po.setDiscountTotal(value(amount.getDiscountTotal()));
        po.setSubsidyTotal(value(amount.getSubsidyTotal()));
        po.setFinanceDiscountTotal(value(amount.getFinanceDiscountTotal()));
        po.setDealPriceTotal(value(amount.getDealPriceTotal()));
        po.setDepositAmount(value(amount.getDepositAmount()));
        po.setDownPaymentAmount(value(amount.getDownPaymentAmount()));
        po.setTailPaymentAmount(value(amount.getTailPaymentAmount()));
        po.setPaidTotal(value(amount.getPaidTotal()));
        po.setRefundTotal(value(amount.getRefundTotal()));
        po.setReceivableTotal(value(amount.getReceivableTotal()));
        po.setNetReceivableTotal(value(amount.getNetReceivableTotal()));
        po.setUnpaidTotal(value(amount.getUnpaidTotal()));
        po.setInvoiceAmount(value(amount.getInvoiceAmount()));
        po.setCalculationVersion(amount.getCalculationVersion());
        po.setUpgradedFromSmall(amount.isUpgradedFromSmall() ? 1 : 0);
        po.setRowValid(1);
        return po;
    }

    private static Money money(java.math.BigDecimal value) {
        return new Money(value == null ? java.math.BigDecimal.ZERO : value);
    }

    private static java.math.BigDecimal value(Money money) {
        return money == null ? java.math.BigDecimal.ZERO : money.getAmount();
    }
}
