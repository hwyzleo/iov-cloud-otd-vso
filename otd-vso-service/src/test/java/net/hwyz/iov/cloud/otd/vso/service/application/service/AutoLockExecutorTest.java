package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 自动锁单测试（US-007 / §14：48 小时边界、非法状态、手动/自动幂等）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("自动锁单 - 48 小时边界/状态校验/幂等")
class AutoLockExecutorTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderAmountRepository orderAmountRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;

    @InjectMocks
    private AutoLockExecutor autoLockExecutor;

    @Test
    @DisplayName("定金支付 47:59：不自动锁单")
    void 未到48小时_不锁单() {
        Order order = downPaymentPaidOrder(47, 59);
        when(orderRepository.findByOrderNo("SO001")).thenReturn(Optional.of(order));

        autoLockExecutor.lockExpiredOrder("SO001");

        verify(orderRepository, never()).save(any());
        assertEquals(OrderState.DOWN_PAYMENT_PAID, order.getOrderState());
    }

    @Test
    @DisplayName("定金支付 48:00：自动锁单并记录时间线")
    void 达到48小时_自动锁单() {
        Order order = downPaymentPaidOrder(48, 1);
        when(orderRepository.findByOrderNo("SO001")).thenReturn(Optional.of(order));
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());

        autoLockExecutor.lockExpiredOrder("SO001");

        assertEquals(OrderState.ARRANGE_PRODUCTION, order.getOrderState());
        verify(orderRepository).save(order);
        verify(orderTimelineAppService).record(any(), any(), any(), any(), anyString(), anyString(),
                anyString(), any(), any(), any());
    }

    @Test
    @DisplayName("非 DOWN_PAYMENT_PAID 状态：不锁单")
    void 非法状态_不锁单() {
        Order order = downPaymentPaidOrderWithState(72, 0, OrderState.CANCEL);
        when(orderRepository.findByOrderNo("SO001")).thenReturn(Optional.of(order));

        autoLockExecutor.lockExpiredOrder("SO001");

        verify(orderRepository, never()).save(any());
        assertEquals(OrderState.CANCEL, order.getOrderState());
    }

    @Test
    @DisplayName("已锁单订单再次执行：幂等，不重复保存")
    void 已锁单幂等() {
        Order order = downPaymentPaidOrderWithState(72, 0, OrderState.ARRANGE_PRODUCTION);
        when(orderRepository.findByOrderNo("SO001")).thenReturn(Optional.of(order));
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());

        autoLockExecutor.lockExpiredOrder("SO001");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("downPaymentTime 为空：不锁单")
    void 缺少支付时间_不锁单() {
        Order order = Order.builder()
                .id("OID1").orderNo("SO001").orderType(OrderType.FORMAL)
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .build();
        when(orderRepository.findByOrderNo("SO001")).thenReturn(Optional.of(order));

        autoLockExecutor.lockExpiredOrder("SO001");

        verify(orderRepository, never()).save(any());
    }

    private Order downPaymentPaidOrderWithState(int hoursAgo, int minutesAgo, OrderState state) {
        Order order = downPaymentPaidOrder(hoursAgo, minutesAgo);
        return Order.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .orderType(order.getOrderType())
                .orderState(state)
                .downPaymentTime(order.getDownPaymentTime())
                .build();
    }

    private Order downPaymentPaidOrder(int hoursAgo, int minutesAgo) {
        Date paidAt = Date.from(LocalDateTime.now().minusHours(hoursAgo).minusMinutes(minutesAgo)
                .atZone(java.time.ZoneOffset.UTC).toInstant());
        return Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderType(OrderType.FORMAL)
                .orderState(OrderState.DOWN_PAYMENT_PAID)
                .downPaymentTime(paidAt)
                .build();
    }
}
