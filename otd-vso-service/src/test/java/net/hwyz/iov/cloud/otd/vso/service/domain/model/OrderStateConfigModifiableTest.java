package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 可改配状态一致性测试（US-012 / MPT 可改配列表）
 * <p>
 * MPT 可改配列表筛选与 C 端/MPT 改配校验必须共用同一事实，否则会出现“列表有但提交被拒”或“可改配却漏单”。
 *
 * @author VSO Team
 */
@DisplayName("订单状态 - 可改配状态唯一事实源")
class OrderStateConfigModifiableTest {

    @Test
    @DisplayName("可改配状态为 210/300/310，且与筛选值列表一致")
    void 可改配状态集合() {
        List<Integer> values = OrderState.configModifiableStateValues();
        assertEquals(3, values.size());
        assertTrue(values.containsAll(Arrays.asList(210, 300, 310)));
        assertEquals(values.size(), OrderState.configModifiableStateValues().stream().distinct().count());
    }

    @Test
    @DisplayName("已锁单/已交付/已取消等状态不允许改配")
    void 其他状态不允许改配() {
        for (OrderState state : Arrays.asList(OrderState.ARRANGE_PRODUCTION, OrderState.ALLOCATION_VEHICLE,
                OrderState.PREPARE_DELIVER, OrderState.DELIVERED, OrderState.ACTIVATED,
                OrderState.CANCEL, OrderState.CLOSED, OrderState.EARNEST_MONEY_UNPAID,
                OrderState.REFUND_APPLY)) {
            assertFalse(state.isConfigModifiable(), state + " 不应允许改配");
        }
        for (OrderState state : Arrays.asList(OrderState.EARNEST_MONEY_PAID,
                OrderState.DOWN_PAYMENT_UNPAID, OrderState.DOWN_PAYMENT_PAID)) {
            assertTrue(state.isConfigModifiable(), state + " 应允许改配");
        }
    }
}
