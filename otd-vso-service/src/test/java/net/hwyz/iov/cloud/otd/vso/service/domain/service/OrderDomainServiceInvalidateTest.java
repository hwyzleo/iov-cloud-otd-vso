package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 小订单失效守卫测试（OTD-VSO-DSN-CR-005 INV-008）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单失效 - 仅 200 可置 EXPIRED")
class OrderDomainServiceInvalidateTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private OrderDomainService orderDomainService;

    @BeforeEach
    void setUp() {
        lenient().when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(orderAmountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(auditRepository.saveTimeline(any(OrderTimelinePo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Order orderWithState(OrderState state) {
        return Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderType(OrderType.SMALL)
                .orderState(state)
                .orderAmount(new OrderAmount("AMT1"))
                .build();
    }

    @Test
    @DisplayName("200 超时失效成功并写 ORDER_INVALIDATE 时间线")
    void 待支付订单可失效() {
        Order order = orderWithState(OrderState.EARNEST_MONEY_UNPAID);
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));

        assertTrue(orderDomainService.invalidateSmallOrder("OID1"));
        assertEquals(OrderState.EXPIRED, order.getOrderState());
        verify(orderRepository).save(order);
        verify(auditRepository).saveTimeline(any(OrderTimelinePo.class));
    }

    @Test
    @DisplayName("已支付订单不得被失效：状态不变、不落库、不写状态变更时间线")
    void 已支付订单跳过失效() {
        Order order = orderWithState(OrderState.EARNEST_MONEY_PAID);
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));

        assertFalse(orderDomainService.invalidateSmallOrder("OID1"));
        assertEquals(OrderState.EARNEST_MONEY_PAID, order.getOrderState());
        verify(orderRepository, never()).save(any(Order.class));
        verify(auditRepository, never()).saveTimeline(any(OrderTimelinePo.class));
    }

    @Test
    @DisplayName("取消/关闭/定金待付等状态不得被失效")
    void 其他状态跳过失效() {
        for (OrderState state : new OrderState[]{OrderState.CANCEL, OrderState.CLOSED,
                OrderState.DOWN_PAYMENT_UNPAID, OrderState.DOWN_PAYMENT_PAID}) {
            Order order = orderWithState(state);
            when(orderRepository.findByOrderId("OID" + state.getValue())).thenReturn(Optional.of(order));

            assertFalse(orderDomainService.invalidateSmallOrder("OID" + state.getValue()),
                    "状态 " + state + " 不应被置为失效");
            assertEquals(state, order.getOrderState());
        }
        verify(orderRepository, never()).save(any(Order.class));
        verify(auditRepository, never()).saveTimeline(any(OrderTimelinePo.class));
    }
}
