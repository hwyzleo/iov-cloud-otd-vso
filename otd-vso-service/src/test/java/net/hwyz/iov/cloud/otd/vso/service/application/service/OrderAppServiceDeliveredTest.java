package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoBaseException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 管理后台/内部服务确认交付测试（OTD-VSO-DSN-CR-015）
 * <p>
 * 覆盖：orderId/orderNo 定位、同一业务订单键互斥、锁内重检、620→650、650 幂等、
 * 600 拒绝、操作上下文透传及提醒任务。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("确认交付 - 定位/互斥/幂等/状态守卫")
class OrderAppServiceDeliveredTest {

    private static final String ORDER_ID = "OID1";
    private static final String ORDER_NO = "SO001";

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private DeliveryAppService deliveryAppService;
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

    @Test
    @DisplayName("MPT 按订单业务 ID 交付：620→650 并透传操作上下文")
    void MPT按订单ID交付() {
        Order order = order(OrderState.FINAL_PAYMENT_PAID);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.delivered(DeliveredCmd.builder()
                .orderId(ORDER_ID)
                .operatorId("mpt-user")
                .operatorRole("mpt_operator")
                .operateSource("mpt")
                .deliveryRemark("车辆及资料已现场移交")
                .build());

        assertEquals(OrderState.DELIVERED, order.getOrderState());
        verify(orderLockService).executeWithLock(eq(ORDER_ID), eq("mpt-user"), eq("delivered"), any(Runnable.class));
        verify(deliveryAppService).completeDelivery(order, "mpt-user", "mpt_operator", "mpt",
                "车辆及资料已现场移交");
        verify(orderRepository).save(order);
        verify(timeoutNotifyService).cancelByOrderIdAndType(ORDER_ID, "PREPARE_DELIVER_TIMEOUT");
        verify(timeoutNotifyService).createTimeoutTask(ORDER_ID, "DELIVERED_TIMEOUT", "remind", 4320);
    }

    @Test
    @DisplayName("内部服务按订单号交付：使用同一业务订单锁键")
    void 内部按订单号交付() {
        Order order = order(OrderState.FINAL_PAYMENT_PAID);
        when(orderRepository.findByOrderNo(ORDER_NO)).thenReturn(Optional.of(order));
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.delivered(DeliveredCmd.builder()
                .orderNo(ORDER_NO)
                .operatorId("system")
                .operatorRole("service")
                .operateSource("delivery_service")
                .deliveryRemark("外部交付服务确认已交付")
                .build());

        assertEquals(OrderState.DELIVERED, order.getOrderState());
        verify(orderLockService).executeWithLock(eq(ORDER_ID), eq("system"), eq("delivered"), any(Runnable.class));
        verify(deliveryAppService).completeDelivery(order, "system", "service", "delivery_service",
                "外部交付服务确认已交付");
    }

    @Test
    @DisplayName("650 幂等：不再写记录、不再推进状态、不再动提醒任务")
    void 已交付幂等() {
        Order order = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        orderAppService.delivered(DeliveredCmd.builder().orderId(ORDER_ID).build());

        verify(deliveryAppService, never()).completeDelivery(any(), anyString(), anyString(), anyString(), any());
        verify(orderRepository, never()).save(any());
        verify(timeoutNotifyService, never()).cancelByOrderIdAndType(anyString(), anyString());
        verify(timeoutNotifyService, never()).createTimeoutTask(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("600 不允许交付：ORDER_STATE_NOT_ALLOWED")
    void 非620拒绝交付() {
        Order order = order(OrderState.PREPARE_DELIVER);
        when(orderRepository.findByOrderId(ORDER_ID)).thenReturn(Optional.of(order));

        assertThrows(OrderStateNotAllowedException.class,
                () -> orderAppService.delivered(DeliveredCmd.builder().orderId(ORDER_ID).build()));

        verify(deliveryAppService, never()).completeDelivery(any(), anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("锁内重检：定位后已被其他请求推进到 650，不重复写事实")
    void 锁内重检幂等() {
        Order located = order(OrderState.FINAL_PAYMENT_PAID);
        Order reloaded = order(OrderState.DELIVERED);
        when(orderRepository.findByOrderId(ORDER_ID))
                .thenReturn(Optional.of(located))
                .thenReturn(Optional.of(reloaded));

        orderAppService.delivered(DeliveredCmd.builder().orderId(ORDER_ID).build());

        verify(deliveryAppService, never()).completeDelivery(any(), anyString(), anyString(), anyString(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("orderId 与 orderNo 必须且只能提供一个")
    void 定位标识互斥() {
        assertThrows(VsoBaseException.class, () -> orderAppService.delivered(DeliveredCmd.builder().build()));
        assertThrows(VsoBaseException.class, () -> orderAppService.delivered(DeliveredCmd.builder()
                .orderId(ORDER_ID).orderNo(ORDER_NO).build()));
        verify(orderRepository, never()).findByOrderId(anyString());
        verify(orderRepository, never()).findByOrderNo(anyString());
    }

    @Test
    @DisplayName("订单不存在返回 ORDER_NOT_EXIST")
    void 订单不存在() {
        when(orderRepository.findByOrderId("MISSING")).thenReturn(Optional.empty());

        assertThrows(OrderNotExistException.class,
                () -> orderAppService.delivered(DeliveredCmd.builder().orderId("MISSING").build()));
    }

    private Order order(OrderState state) {
        return Order.builder()
                .id(ORDER_ID)
                .orderNo(ORDER_NO)
                .orderType(OrderType.FORMAL)
                .orderState(state)
                .deliveryStoreCode("STORE001")
                .deliveryVin("VIN001")
                .build();
    }
}
