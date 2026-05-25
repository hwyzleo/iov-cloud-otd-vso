package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
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
        Money price = Money.of(new BigDecimal("100000"), "CNY");

        OrderAmount orderAmount = new OrderAmount("test-amount-id");
        orderAmount.setVehiclePrice(price);
        orderAmount.setOptionPrice(Money.ZERO_CNY);
        orderAmount.setPaidTotal(Money.of(new BigDecimal("10000"), "CNY"));

        return Order.builder()
                .id("test-order-id")
                .orderNo("TEST000001")
                .orderType(OrderType.FORMAL)
                .orderState(state)
                .orderAmount(orderAmount)
                .build();
    }
}
