package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 订单状态机测试（OTD-VSO-DSN-CR-002 §9.1 必测）
 *
 * @author VSO Team
 */
@DisplayName("订单状态机 - 主链/退款/非法迁移/激活限制")
class OrderStateMachineTest {

    @Test
    @DisplayName("完整主链：意向金→定金→锁单→配车→发运→运输→交付→激活")
    void 完整主链状态迁移合法() {
        int[] chain = {
                OrderState.EARNEST_MONEY_UNPAID.getValue(),
                OrderState.EARNEST_MONEY_PAID.getValue(),
                OrderState.DOWN_PAYMENT_PAID.getValue(),
                OrderState.ARRANGE_PRODUCTION.getValue(),
                OrderState.ALLOCATION_VEHICLE.getValue(),
                OrderState.APPLY_TRANSPORT.getValue(),
                OrderState.PREPARE_TRANSPORT.getValue(),
                OrderState.TRANSPORTING.getValue(),
                OrderState.PREPARE_DELIVER.getValue(),
                OrderState.DELIVERED.getValue(),
                OrderState.ACTIVATED.getValue()
        };
        for (int i = 0; i < chain.length - 1; i++) {
            int from = chain[i];
            int to = chain[i + 1];
            assertTrue(OrderStateMachine.canTransition(from, to, OrderType.SMALL),
                    "状态 " + from + " -> " + to + " 应合法");
        }
    }

    @Test
    @DisplayName("退款主链：退款申请→退款完成")
    void 退款状态迁移合法() {
        assertTrue(OrderStateMachine.canTransition(OrderState.EARNEST_MONEY_PAID.getValue(),
                OrderState.REFUND_APPLY.getValue(), OrderType.SMALL));
        assertTrue(OrderStateMachine.canTransition(OrderState.DOWN_PAYMENT_PAID.getValue(),
                OrderState.REFUND_APPLY.getValue(), OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(OrderState.REFUND_APPLY.getValue(),
                OrderState.REFUND_COMPLETE.getValue(), OrderType.FORMAL));
    }

    @Test
    @DisplayName("非法跨状态迁移被拒绝")
    void 非法跨状态迁移被拒绝() {
        assertFalse(OrderStateMachine.canTransition(OrderState.EARNEST_MONEY_UNPAID.getValue(),
                OrderState.ACTIVATED.getValue(), OrderType.SMALL));
        assertFalse(OrderStateMachine.canTransition(OrderState.DOWN_PAYMENT_PAID.getValue(),
                OrderState.TRANSPORTING.getValue(), OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(OrderState.ARRANGE_PRODUCTION.getValue(),
                OrderState.DELIVERED.getValue(), OrderType.FORMAL));
        assertThrows(IllegalStateException.class, () ->
                OrderStateMachine.validateTransition(OrderState.EARNEST_MONEY_UNPAID.getValue(),
                        OrderState.ACTIVATED.getValue(), OrderType.SMALL));
    }

    @Test
    @DisplayName("激活仅允许 DELIVERED → ACTIVATED")
    void 激活仅允许已交付状态() {
        Order delivered = Order.builder()
                .orderNo("SO-ACT-1")
                .orderType(OrderType.FORMAL)
                .orderState(OrderState.DELIVERED)
                .build();
        assertDoesNotThrow(delivered::activate);
        assertTrue(delivered.getOrderState() == OrderState.ACTIVATED);

        Order paid = Order.builder()
                .orderNo("SO-ACT-2")
                .orderType(OrderType.FORMAL)
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .build();
        assertThrows(IllegalStateException.class, paid::activate);
    }
}
