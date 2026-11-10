package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单金额固定样例测试（OTD-VSO-REQ-CR-002 §8 / DSN-CR-002 §14）
 *
 * @author VSO Team
 */
@DisplayName("订单金额 - 固定样例/手续费/超额退款")
class OrderAmountC002Test {

    private OrderAmount amount;

    @BeforeEach
    void setUp() {
        amount = new OrderAmount("AMT1");
        amount.setVehiclePrice(new Money(new BigDecimal("200000.00")));
        amount.setOptionPrice(new Money(new BigDecimal("3000.00")));
        amount.setDepositAmount(new Money(new BigDecimal("5000.00")));
        amount.setDownPaymentAmount(new Money(new BigDecimal("20000.00")));
        amount.setPaidTotal(new Money(BigDecimal.ZERO));
        amount.setRefundTotal(new Money(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("§8.1 选中 Variant + Option A+B：成交总价 203000")
    void 成交总价应为203000() {
        amount.recalculate();
        assertEquals(0, new BigDecimal("203000.00").compareTo(amount.getDealPriceTotal().getAmount()));
    }

    @Test
    @DisplayName("§8.2 意向金订单：定金 5000、应付定金 20000、差额 15000、已支付 0")
    void 意向金订单金额快照() {
        assertEquals(0, new BigDecimal("5000.00").compareTo(amount.getDepositAmount().getAmount()));
        assertEquals(0, new BigDecimal("20000.00").compareTo(amount.getDownPaymentAmount().getAmount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(amount.getPaidTotal().getAmount()));
        assertEquals(0, new BigDecimal("15000.00").compareTo(
                amount.calculateEarnestToDownDifference().getAmount()));
    }

    @Test
    @DisplayName("§8.5 已锁单退款：paidTotal=20000 时 fee=1000、refund=19000")
    void 锁单退款_20000() {
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        Money refund = amount.calculateRefundAmount(OrderState.ARRANGE_PRODUCTION);
        assertEquals(0, new BigDecimal("19000.00").compareTo(refund.getAmount()));
    }

    @Test
    @DisplayName("§8.5 已锁单退款：paidTotal=5000 时 fee=500（最小值）、refund=4500")
    void 锁单退款_5000() {
        amount.setPaidTotal(new Money(new BigDecimal("5000.00")));
        Money refund = amount.calculateRefundAmount(OrderState.ARRANGE_PRODUCTION);
        assertEquals(0, new BigDecimal("4500.00").compareTo(refund.getAmount()));
    }

    @Test
    @DisplayName("未锁单退款：全额退款无手续费")
    void 未锁单退款_全额() {
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        Money refund = amount.calculateRefundAmount(OrderState.DOWN_PAYMENT_PAID);
        assertEquals(0, new BigDecimal("20000.00").compareTo(refund.getAmount()));
        assertEquals("full_refund", amount.getRefundScene(OrderState.DOWN_PAYMENT_PAID));
    }

    @Test
    @DisplayName("§8.4 意向金高于定金：upgradedFromSmall 未锁单退款 10000、手续费 10000")
    void 小定升级超额退款() {
        amount.setDepositAmount(new Money(new BigDecimal("20000.00")));
        amount.setDownPaymentAmount(new Money(new BigDecimal("10000.00")));
        amount.setPaidTotal(new Money(new BigDecimal("20000.00")));
        amount.setUpgradedFromSmall(true);

        Money refund = amount.calculateRefundAmount(OrderState.DOWN_PAYMENT_PAID);
        Money fee = amount.getPaidTotal().subtract(refund);
        assertEquals(0, new BigDecimal("10000.00").compareTo(refund.getAmount()));
        assertEquals(0, new BigDecimal("10000.00").compareTo(fee.getAmount()));
        assertEquals("excess_refund", amount.getRefundScene(OrderState.DOWN_PAYMENT_PAID));
    }

    @Test
    @DisplayName("退款状态白名单：意向金已付/定金已付/已锁单可退，其他拒绝")
    void 可退款状态() {
        assertTrue(amount.canRefund(OrderState.EARNEST_MONEY_PAID));
        assertTrue(amount.canRefund(OrderState.DOWN_PAYMENT_PAID));
        assertTrue(amount.canRefund(OrderState.ARRANGE_PRODUCTION));
        assertFalse(amount.canRefund(OrderState.ALLOCATION_VEHICLE));
        assertFalse(amount.canRefund(OrderState.EARNEST_MONEY_UNPAID));
    }
}
