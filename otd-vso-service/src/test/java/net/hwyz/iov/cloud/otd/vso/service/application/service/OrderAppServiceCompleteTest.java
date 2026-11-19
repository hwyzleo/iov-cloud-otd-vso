package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CompleteOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * MPT 人工办结测试（OTD-VSO-DSN-CR-016）
 * <p>
 * 覆盖：650 成功、900 幂等、其他状态拒绝、订单锁内重检、真实 MPT 操作上下文、
 * ORDER_COMPLETED 单条、提交后取消 DELIVERED_TIMEOUT 与取消失败告警。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("完成订单 - 人工办结主链")
class OrderAppServiceCompleteTest {

    private static final String ORDER_ID = "OID1";
    private static final String ORDER_NO = "SO001";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;
    @Mock
    private TimeoutNotifyService timeoutNotifyService;

    @InjectMocks
    private OrderAppService orderAppService;

    @BeforeEach
    void setUp() {
        lenient().doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(3)).run();
            return null;
        }).when(orderLockService).executeWithLock(anyString(), anyString(), anyString(), any(Runnable.class));
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("650 办结成功：写入真实 MPT 上下文与 ORDER_COMPLETED")
    void 办结成功() {
        Order order = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.completeOrder(cmd());

        assertEquals(OrderState.COMPLETED, order.getOrderState());
        verify(orderLockService).executeWithLock(eq(ORDER_ID), eq("mpt-user"), eq("complete"), any(Runnable.class));
        verify(orderRepository).save(order);
        verify(orderTimelineAppService).record(ORDER_ID, OrderTimelineEventType.ORDER_COMPLETED,
                "650", "900", "mpt-user", "mpt_operator", "mpt", null, null, "交付资料及手续已核查");
        verify(timeoutNotifyService).cancelByOrderIdAndType(ORDER_ID, "DELIVERED_TIMEOUT");
    }

    @Test
    @DisplayName("备注为空时写入默认办结说明")
    void 备注为空回退默认() {
        Order order = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.completeOrder(CompleteOrderCmd.builder()
                .orderId(ORDER_ID)
                .operatorId("mpt-user")
                .build());

        verify(orderTimelineAppService).record(ORDER_ID, OrderTimelineEventType.ORDER_COMPLETED,
                "650", "900", "mpt-user", "mpt_operator", "mpt", null, null, "管理后台人工核查完成");
    }

    @Test
    @DisplayName("900 幂等：不保存、不追加时间线、不动提醒")
    void 已办结幂等() {
        Order order = order(OrderState.COMPLETED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.completeOrder(cmd());

        verify(orderRepository, never()).save(any());
        verify(orderTimelineAppService, never()).record(anyString(), any(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), any(), anyString());
        verify(timeoutNotifyService, never()).cancelByOrderIdAndType(anyString(), anyString());
    }

    @Test
    @DisplayName("非 650 状态拒绝办结")
    void 非650拒绝办结() {
        Order order = order(OrderState.FINAL_PAYMENT_PAID);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(OrderStateNotAllowedException.class, () -> orderAppService.completeOrder(cmd()));

        assertEquals(OrderState.FINAL_PAYMENT_PAID, order.getOrderState());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("订单不存在返回 ORDER_NOT_EXIST")
    void 订单不存在() {
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class, () -> orderAppService.completeOrder(cmd()));
    }

    @Test
    @DisplayName("锁内重检：并发重复请求只产生一次办结事实")
    void 并发重复请求只办结一次() {
        Order delivered = order(OrderState.DELIVERED);
        Order completed = order(OrderState.COMPLETED);
        when(orderRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(delivered))
                .thenReturn(Optional.of(completed));

        orderAppService.completeOrder(cmd());
        orderAppService.completeOrder(cmd());

        verify(orderRepository, times(1)).save(any());
        verify(orderTimelineAppService, times(1)).record(eq(ORDER_ID),
                eq(OrderTimelineEventType.ORDER_COMPLETED), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(), anyString());
    }

    @Test
    @DisplayName("DELIVERED_TIMEOUT 在事务提交后取消，提交前不得提前操作 Redis")
    void 提交后取消提醒() {
        TransactionSynchronizationManager.initSynchronization();
        Order order = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.completeOrder(cmd());

        verify(timeoutNotifyService, never()).cancelByOrderIdAndType(anyString(), anyString());
        for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
            synchronization.afterCommit();
        }
        verify(timeoutNotifyService).cancelByOrderIdAndType(ORDER_ID, "DELIVERED_TIMEOUT");
    }

    @Test
    @DisplayName("提醒取消失败仅告警，不影响已提交的办结事实")
    void 取消失败仅告警() {
        Order order = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));
        doThrow(new IllegalStateException("redis down"))
                .when(timeoutNotifyService).cancelByOrderIdAndType(ORDER_ID, "DELIVERED_TIMEOUT");

        orderAppService.completeOrder(cmd());

        assertEquals(OrderState.COMPLETED, order.getOrderState());
        verify(orderRepository).save(order);
        verify(orderTimelineAppService).record(eq(ORDER_ID), eq(OrderTimelineEventType.ORDER_COMPLETED),
                anyString(), anyString(), anyString(), anyString(), anyString(), any(), any(), anyString());
    }

    private CompleteOrderCmd cmd() {
        return CompleteOrderCmd.builder()
                .orderId(ORDER_ID)
                .operatorId("mpt-user")
                .completionRemark("交付资料及手续已核查")
                .build();
    }

    private Order order(OrderState state) {
        return Order.builder()
                .id(ORDER_ID)
                .orderNo(ORDER_NO)
                .orderType(OrderType.FORMAL)
                .orderState(state)
                .build();
    }
}
