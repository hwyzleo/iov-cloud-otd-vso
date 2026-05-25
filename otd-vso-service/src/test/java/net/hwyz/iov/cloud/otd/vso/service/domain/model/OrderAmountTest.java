package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderAmountTest {

    @Test
    void calculateRefundAmount_earliestMoneyPaid_shouldReturnFullRefund() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        orderAmount.setPaidTotal(new Money(new BigDecimal("10000.00")));
        
        Money refundAmount = orderAmount.calculateRefundAmount(OrderState.EARNEST_MONEY_PAID);
        
        assertEquals(new BigDecimal("10000.00"), refundAmount.getAmount());
    }

    @Test
    void calculateRefundAmount_downPaymentPaid_shouldReturnFullRefund() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        orderAmount.setPaidTotal(new Money(new BigDecimal("50000.00")));
        
        Money refundAmount = orderAmount.calculateRefundAmount(OrderState.DOWN_PAYMENT_PAID);
        
        assertEquals(new BigDecimal("50000.00"), refundAmount.getAmount());
    }

    @Test
    void calculateRefundAmount_arrangeProduction_shouldReturnPartialRefund() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        orderAmount.setPaidTotal(new Money(new BigDecimal("50000.00")));
        
        Money refundAmount = orderAmount.calculateRefundAmount(OrderState.ARRANGE_PRODUCTION);
        
        // 手续费 = max(50000 * 5%, 500) = 2500
        // 退款金额 = 50000 - 2500 = 47500
        assertEquals(0, new BigDecimal("47500.00").compareTo(refundAmount.getAmount()));
    }

    @Test
    void calculateRefundAmount_arrangeProduction_lowAmount_shouldUseMinFee() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        orderAmount.setPaidTotal(new Money(new BigDecimal("5000.00")));
        
        Money refundAmount = orderAmount.calculateRefundAmount(OrderState.ARRANGE_PRODUCTION);
        
        // 手续费 = max(5000 * 5%, 500) = 500 (最低手续费)
        // 退款金额 = 5000 - 500 = 4500
        assertEquals(new BigDecimal("4500.00"), refundAmount.getAmount());
    }

    @Test
    void calculateRefundAmount_allocationVehicle_shouldThrowException() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        orderAmount.setPaidTotal(new Money(new BigDecimal("50000.00")));
        
        assertThrows(IllegalStateException.class, () -> {
            orderAmount.calculateRefundAmount(OrderState.ALLOCATION_VEHICLE);
        });
    }

    @Test
    void canRefund_earliestMoneyPaid_shouldReturnTrue() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertTrue(orderAmount.canRefund(OrderState.EARNEST_MONEY_PAID));
    }

    @Test
    void canRefund_downPaymentPaid_shouldReturnTrue() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertTrue(orderAmount.canRefund(OrderState.DOWN_PAYMENT_PAID));
    }

    @Test
    void canRefund_arrangeProduction_shouldReturnTrue() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertTrue(orderAmount.canRefund(OrderState.ARRANGE_PRODUCTION));
    }

    @Test
    void canRefund_allocationVehicle_shouldReturnFalse() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertFalse(orderAmount.canRefund(OrderState.ALLOCATION_VEHICLE));
    }

    @Test
    void getRefundScene_earliestMoneyPaid_shouldReturnFullRefund() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertEquals("full_refund", orderAmount.getRefundScene(OrderState.EARNEST_MONEY_PAID));
    }

    @Test
    void getRefundScene_arrangeProduction_shouldReturnPartialRefund() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertEquals("partial_refund", orderAmount.getRefundScene(OrderState.ARRANGE_PRODUCTION));
    }

    @Test
    void getRefundScene_allocationVehicle_shouldThrowException() {
        OrderAmount orderAmount = new OrderAmount("AMT001");
        assertThrows(IllegalStateException.class, () -> {
            orderAmount.getRefundScene(OrderState.ALLOCATION_VEHICLE);
        });
    }
}
