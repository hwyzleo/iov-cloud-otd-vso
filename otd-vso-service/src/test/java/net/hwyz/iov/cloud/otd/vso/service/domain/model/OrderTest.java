package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void canRefund_earliestMoneyPaid_shouldReturnTrue() {
        Order order = createOrderWithState(OrderState.EARNEST_MONEY_PAID);
        assertTrue(order.canRefund());
    }

    @Test
    void canRefund_downPaymentPaid_shouldReturnTrue() {
        Order order = createOrderWithState(OrderState.DOWN_PAYMENT_PAID);
        assertTrue(order.canRefund());
    }

    @Test
    void canRefund_arrangeProduction_shouldReturnTrue() {
        Order order = createOrderWithState(OrderState.ARRANGE_PRODUCTION);
        assertTrue(order.canRefund());
    }

    @Test
    void canRefund_allocationVehicle_shouldReturnFalse() {
        Order order = createOrderWithState(OrderState.ALLOCATION_VEHICLE);
        assertFalse(order.canRefund());
    }

    @Test
    void requestRefund_earliestMoneyPaid_shouldSetStateToRefundApply() {
        Order order = createOrderWithState(OrderState.EARNEST_MONEY_PAID);
        order.requestRefund();
        assertEquals(OrderState.REFUND_APPLY, order.getOrderState());
    }

    @Test
    void requestRefund_allocationVehicle_shouldThrowException() {
        Order order = createOrderWithState(OrderState.ALLOCATION_VEHICLE);
        assertThrows(IllegalStateException.class, () -> {
            order.requestRefund();
        });
    }

    private Order createOrderWithState(OrderState state) {
        Order order = new Order();
        order.setOrderState(state);
        order.setOrderType(OrderType.FORMAL);
        return order;
    }
}
