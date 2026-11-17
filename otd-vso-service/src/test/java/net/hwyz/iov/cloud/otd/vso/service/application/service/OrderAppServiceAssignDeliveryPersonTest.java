package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignDeliveryPersonCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 分派交付人员测试（OTD-VSO-DSN-CR-010）
 * <p>
 * 传入的是对外订单号（orderNo，形如 VSO2611130001），必须按 order_no 加载订单；
 * 首次分配与改派各写一条 {@code DELIVERY_PERSON_ASSIGNED} 时间线事件，同一人员重复提交不产生留痕。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分派交付人员 - 按订单号加载订单并写时间线")
class OrderAppServiceAssignDeliveryPersonTest {

    private static final String ORDER_NO = "VSO2611130001";

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;

    @InjectMocks
    private OrderAppService orderAppService;

    @Test
    @DisplayName("首次分配：按订单号加载订单并写入一条时间线事件")
    void 首次分配写时间线事件() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .orderState(OrderState.PREPARE_DELIVER)
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三")
                .operatorId("admin")
                .build());

        verify(orderDomainService).loadOrderByOrderNo(ORDER_NO);
        verify(orderDomainService, never()).loadOrder(anyString());
        verify(orderRepository).save(order);
        assertEquals("1001", order.getDeliveryPersonId());
        assertEquals("张三", order.getDeliveryPersonName());

        ArgumentCaptor<String> statusCaptor = ArgumentCaptor.forClass(String.class);
        verify(orderTimelineAppService).record(eq("ORD1731580800000"),
                eq(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED),
                statusCaptor.capture(), statusCaptor.capture(), eq("admin"), eq("mpt_operator"), eq("mpt"),
                isNull(), isNull(), eq("分配交付人员：张三(1001)"));
        assertEquals(2, statusCaptor.getAllValues().size(), "beforeStatus 与 afterStatus 均应传入");
        assertEquals(statusCaptor.getAllValues().get(0), statusCaptor.getAllValues().get(1),
                "分配交付人员不得变更订单状态（CR-010 INV-023）");
        assertEquals(String.valueOf(OrderState.PREPARE_DELIVER.getValue()), statusCaptor.getValue());
    }

    @Test
    @DisplayName("改派：追加第二条事件，备注保留原交付人员与新交付人员")
    void 改派写第二条事件() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .orderState(OrderState.PREPARE_DELIVER)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三")
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1002")
                .deliveryPersonName("李四")
                .operatorId("admin")
                .build());

        verify(orderRepository).save(order);
        assertEquals("1002", order.getDeliveryPersonId());
        verify(orderTimelineAppService).record(eq("ORD1731580800000"),
                eq(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED),
                anyString(), anyString(), eq("admin"), eq("mpt_operator"), eq("mpt"),
                isNull(), isNull(), eq("改派交付人员：李四(1002)，原交付人员：张三(1001)"));
    }

    @Test
    @DisplayName("同一人员重复提交：不保存、不写时间线（幂等）")
    void 同一人员重复提交幂等() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三")
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三")
                .operatorId("admin")
                .build());

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderTimelineAppService, never()).record(anyString(), any(), any(), any(), any(), any(),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("同一人员仅姓名变更：更新展示但不写时间线")
    void 同一人员姓名变更仅更新展示() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三")
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .deliveryPersonName("张三（改名）")
                .operatorId("admin")
                .build());

        verify(orderRepository).save(order);
        assertEquals("张三（改名）", order.getDeliveryPersonName());
        verify(orderTimelineAppService, never()).record(anyString(), any(), any(), any(), any(), any(),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("订单不存在：抛 ORDER_NOT_EXIST 且不写时间线")
    void 订单不存在不写事件() {
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO))
                .thenThrow(new OrderNotExistException(ORDER_NO));

        assertThrows(OrderNotExistException.class, () -> orderAppService.assignDeliveryPerson(
                AssignDeliveryPersonCmd.builder()
                        .orderNo(ORDER_NO)
                        .deliveryPersonId("1001")
                        .deliveryPersonName("张三")
                        .operatorId("admin")
                        .build()));

        verify(orderRepository, times(0)).save(any(Order.class));
        verify(orderTimelineAppService, never()).record(anyString(), any(), any(), any(), any(), any(),
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("首次分配且无姓名时备注回退为工号，不输出 null 字面量")
    void 姓名缺失备注回退工号() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .orderState(OrderState.PREPARE_DELIVER)
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1001")
                .operatorId("admin")
                .build());

        ArgumentCaptor<String> remark = ArgumentCaptor.forClass(String.class);
        verify(orderTimelineAppService).record(anyString(), eq(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED),
                any(), any(), anyString(), anyString(), anyString(), any(), any(), remark.capture());
        assertTrue(remark.getValue().contains("1001"));
        assertTrue(!remark.getValue().contains("null"));
    }
}
