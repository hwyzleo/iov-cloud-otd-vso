package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderTimelineRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 订单时间线可见性测试（OTD-VSO-DSN-CR-010 INV-022）
 * <p>
 * MPT 查询返回全量事件；C 端查询必须过滤内部运营事件（如交付人员分配）。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单时间线 - MPT 全量 / C 端过滤内部事件")
class OrderTimelineAppServiceTest {

    private static final String ORDER_ID = "ORD1731580800000";

    @Mock
    private OrderTimelineRepository orderTimelineRepository;

    @InjectMocks
    private OrderTimelineAppService orderTimelineAppService;

    private OrderTimeline event(OrderTimelineEventType type, String remark) {
        return OrderTimeline.builder()
                .timelineId("TL-" + type.getCode())
                .orderId(ORDER_ID)
                .eventType(type.getCode())
                .eventName(type.getName())
                .beforeStatus("400")
                .afterStatus("400")
                .operatorId("admin")
                .operatorRole("mpt_operator")
                .operateSource("mpt")
                .eventRemark(remark)
                .build();
    }

    @Test
    @DisplayName("MPT 时间线返回全量事件，包含交付人员分配")
    void MPT时间线包含交付人员分配() {
        when(orderTimelineRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(
                event(OrderTimelineEventType.VEHICLE_ASSIGNED, "配车"),
                event(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED, "分配交付人员：张三(1001)")));

        List<OrderTimelineResult> result = orderTimelineAppService.timelineOf(ORDER_ID);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r ->
                OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED.getCode().equals(r.getEventType())));
    }

    @Test
    @DisplayName("C 端时间线过滤交付人员分配事件")
    void C端时间线过滤交付人员分配() {
        when(orderTimelineRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(
                event(OrderTimelineEventType.VEHICLE_ASSIGNED, "配车"),
                event(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED, "分配交付人员：张三(1001)"),
                event(OrderTimelineEventType.PREPARE_DELIVERY, "待交付")));

        List<OrderTimelineResult> result = orderTimelineAppService.customerTimelineOf(ORDER_ID);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(r ->
                OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED.getCode().equals(r.getEventType())));
        assertEquals(OrderTimelineEventType.VEHICLE_ASSIGNED.getCode(), result.get(0).getEventType());
        assertEquals(OrderTimelineEventType.PREPARE_DELIVERY.getCode(), result.get(1).getEventType());
    }

    @Test
    @DisplayName("仅有内部事件时 C 端返回空列表")
    void 仅内部事件时C端为空() {
        when(orderTimelineRepository.findByOrderId(ORDER_ID)).thenReturn(List.of(
                event(OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED, "分配交付人员：张三(1001)")));

        assertTrue(orderTimelineAppService.customerTimelineOf(ORDER_ID).isEmpty());
    }

    @Test
    @DisplayName("无事件时 C 端与 MPT 均返回空列表")
    void 无事件返回空列表() {
        when(orderTimelineRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        assertTrue(orderTimelineAppService.timelineOf(ORDER_ID).isEmpty());
        assertTrue(orderTimelineAppService.customerTimelineOf(ORDER_ID).isEmpty());
    }
}
