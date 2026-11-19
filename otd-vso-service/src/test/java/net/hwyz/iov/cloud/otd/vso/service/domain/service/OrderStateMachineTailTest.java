package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 尾款与交付状态机测试（OTD-VSO-DSN-CR-014 US-034 / INV-045）
 * <p>
 * 允许 600→620→650→700；拒绝 600→650；保留 600/620→950 与 650→700。
 *
 * @author VSO Team
 */
@DisplayName("订单状态机 - 尾款与交付链")
class OrderStateMachineTailTest {

    @Test
    @DisplayName("允许 600 → 620 → 650 → 700")
    void 允许尾款交付激活链() {
        assertTrue(OrderStateMachine.canTransition(600, 620, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(620, 650, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(650, 700, OrderType.FORMAL));
    }

    @Test
    @DisplayName("拒绝 600 直接交付 650")
    void 拒绝未付尾款直接交付() {
        assertFalse(OrderStateMachine.canTransition(600, 650, OrderType.FORMAL));
        assertThrows(IllegalStateException.class,
                () -> OrderStateMachine.validateTransition(600, 650, OrderType.FORMAL));
    }

    @Test
    @DisplayName("保留 600/620 → 950 取消边界")
    void 保留取消边界() {
        assertTrue(OrderStateMachine.canTransition(600, 950, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(620, 950, OrderType.FORMAL));
    }

    @Test
    @DisplayName("620 不允许跳回 600 或直接激活 700")
    void 禁止非法回退与越级激活() {
        assertFalse(OrderStateMachine.canTransition(620, 600, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(620, 700, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(650, 700, OrderType.FORMAL));
    }

    @Test
    @DisplayName("尾款状态常量保持 620，交付 650，激活 700")
    void 状态值稳定() {
        assertTrue(OrderState.FINAL_PAYMENT_PAID.getValue() == 620);
        assertTrue(OrderState.DELIVERED.getValue() == 650);
        assertTrue(OrderState.ACTIVATED.getValue() == 700);
    }
}
