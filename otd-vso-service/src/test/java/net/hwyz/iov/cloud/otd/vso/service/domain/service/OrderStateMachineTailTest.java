package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 尾款、交付与办结状态机测试（OTD-VSO-DSN-CR-014 US-034 / OTD-VSO-DSN-CR-016）
 * <p>
 * 允许 600→620→650→900；拒绝 600→650 与任何 700 迁移；保留 600/620/650→950。
 *
 * @author VSO Team
 */
@DisplayName("订单状态机 - 尾款与交付/办结链")
class OrderStateMachineTailTest {

    @Test
    @DisplayName("允许 600 → 620 → 650 → 900")
    void 允许尾款交付办结链() {
        assertTrue(OrderStateMachine.canTransition(600, 620, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(620, 650, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(650, 900, OrderType.FORMAL));
    }

    @Test
    @DisplayName("拒绝 600 直接交付 650")
    void 拒绝未付尾款直接交付() {
        assertFalse(OrderStateMachine.canTransition(600, 650, OrderType.FORMAL));
        assertThrows(IllegalStateException.class,
                () -> OrderStateMachine.validateTransition(600, 650, OrderType.FORMAL));
    }

    @Test
    @DisplayName("保留 600/620/650 → 950 取消边界")
    void 保留取消边界() {
        assertTrue(OrderStateMachine.canTransition(600, 950, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(620, 950, OrderType.FORMAL));
        assertTrue(OrderStateMachine.canTransition(650, 950, OrderType.FORMAL));
    }

    @Test
    @DisplayName("拒绝任何状态→700 与 700→其他状态，非 650 不得办结")
    void 拒绝激活与越级办结() {
        assertFalse(OrderStateMachine.canTransition(620, 700, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(650, 700, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(700, 650, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(700, 900, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(620, 900, OrderType.FORMAL));
        assertFalse(OrderStateMachine.canTransition(600, 900, OrderType.FORMAL));
    }

    @Test
    @DisplayName("尾款状态常量保持 620，交付 650，办结 900，且 700 已删除")
    void 状态值稳定() {
        assertTrue(OrderState.FINAL_PAYMENT_PAID.getValue() == 620);
        assertTrue(OrderState.DELIVERED.getValue() == 650);
        assertTrue(OrderState.COMPLETED.getValue() == 900);
        assertTrue(OrderState.valOf(700) == null);
    }
}
