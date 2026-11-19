package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointmentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 确认交付集成测试（OTD-VSO-DSN-CR-015）
 * <p>
 * 真实本地库事务内验证 MPT 620→650、预约完成与关联、无预约直建记录、
 * 服务端时间、操作上下文留痕、650 幂等及非 620 拒绝。
 * 本测试不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("确认交付 - 集成测试")
class OrderAppServiceDeliveredIntegrationTest {

    private static final String EVENT_TYPE = OrderTimelineEventType.ORDER_DELIVERED.getCode();

    @Autowired
    private OrderAppService orderAppService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;

    @MockBean
    private ConfigurationService configurationService;

    private String orderId;
    private String orderNo;

    @BeforeEach
    void setUp() {
        String suffix = String.valueOf(System.nanoTime());
        orderId = "ORD_DLV_" + suffix;
        orderNo = "VSO_DLV_" + suffix;
    }

    @Test
    @DisplayName("MPT 确认交付：620→650、完成预约、单条记录与 MPT 时间线，重复提交幂等")
    void MPT确认交付完整事实() {
        saveOrder(OrderState.FINAL_PAYMENT_PAID);
        String appointmentNo = saveBookedAppointment();

        orderAppService.delivered(mptCommand("车辆及资料已现场移交"));

        Order order = orderRepository.findByOrderId(orderId).orElseThrow();
        assertEquals(OrderState.DELIVERED, order.getOrderState());
        assertEquals(DeliveryAppointmentStatus.COMPLETED,
                deliveryRepository.findByAppointmentNo(appointmentNo).orElseThrow().getAppointmentStatus());

        DeliveryRecord record = deliveryRepository.findRecordByOrderId(orderId).orElseThrow();
        assertEquals("delivered", record.getDeliveryStatus());
        assertEquals("STORE001", record.getDeliveryStoreCode());
        assertEquals("VIN001", record.getVehicleVin());
        assertEquals("A", record.getDeliveryPlace());
        assertEquals("车辆及资料已现场移交", record.getDeliveryRemark());
        assertNotNull(record.getActualDeliveryTime(), "实际交付时间必须由服务端生成");
        assertNotNull(record.getDeliveryAppointmentId(), "有有效预约时必须关联预约");

        List<OrderTimelineResult> deliveredEvents = deliveredEvents();
        assertEquals(1, deliveredEvents.size(), "ORDER_DELIVERED 只允许一条有效时间线");
        OrderTimelineResult event = deliveredEvents.get(0);
        assertEquals("620", event.getBeforeStatus());
        assertEquals("650", event.getAfterStatus());
        assertEquals("mpt-user", event.getOperatorId());
        assertEquals("mpt_operator", event.getOperatorRole());
        assertEquals("mpt", event.getOperateSource());
        assertEquals(record.getDeliveryRecordId(), event.getRelatedDocNo());

        // 650 幂等：不重复写记录/时间线，不再次推进状态
        orderAppService.delivered(mptCommand("重复提交"));
        assertEquals(OrderState.DELIVERED, orderRepository.findByOrderId(orderId).orElseThrow().getOrderState());
        assertEquals(1, deliveredEvents().size(), "650 重复提交不得追加时间线");
        assertEquals(record.getDeliveryRecordId(),
                deliveryRepository.findRecordByOrderId(orderId).orElseThrow().getDeliveryRecordId());
    }

    @Test
    @DisplayName("内部兼容入口：无预约直接建记录，保留 service/delivery_service 来源")
    void 内部入口无预约交付() {
        saveOrder(OrderState.FINAL_PAYMENT_PAID);

        orderAppService.delivered(DeliveredCmd.builder()
                .orderNo(orderNo)
                .operatorId("system")
                .operatorRole("service")
                .operateSource("delivery_service")
                .deliveryRemark("外部交付服务确认已交付")
                .build());

        assertEquals(OrderState.DELIVERED, orderRepository.findByOrderId(orderId).orElseThrow().getOrderState());
        DeliveryRecord record = deliveryRepository.findRecordByOrderId(orderId).orElseThrow();
        assertNull(record.getDeliveryAppointmentId(), "无有效预约时不得虚构预约关联");
        assertNull(record.getDeliveryPlace(), "无有效预约时交付地点保持空");
        assertEquals("外部交付服务确认已交付", record.getDeliveryRemark());

        List<OrderTimelineResult> deliveredEvents = deliveredEvents();
        assertEquals(1, deliveredEvents.size());
        assertEquals("system", deliveredEvents.get(0).getOperatorId());
        assertEquals("service", deliveredEvents.get(0).getOperatorRole());
        assertEquals("delivery_service", deliveredEvents.get(0).getOperateSource());
    }

    @Test
    @DisplayName("600 拒绝确认交付：状态不变且不落交付事实")
    void 非620拒绝() {
        saveOrder(OrderState.PREPARE_DELIVER);

        assertThrows(OrderStateNotAllowedException.class,
                () -> orderAppService.delivered(mptCommand("提前交付")));

        assertEquals(OrderState.PREPARE_DELIVER, orderRepository.findByOrderId(orderId).orElseThrow().getOrderState());
        assertTrue(deliveryRepository.findRecordByOrderId(orderId).isEmpty(), "非法状态不得写交付记录");
        assertTrue(deliveredEvents().isEmpty(), "非法状态不得写交付时间线");
    }

    private DeliveredCmd mptCommand(String remark) {
        return DeliveredCmd.builder()
                .orderId(orderId)
                .operatorId("mpt-user")
                .operatorRole("mpt_operator")
                .operateSource("mpt")
                .deliveryRemark(remark)
                .build();
    }

    private List<OrderTimelineResult> deliveredEvents() {
        return orderAppService.getTimelineByOrderId(orderId).stream()
                .filter(event -> EVENT_TYPE.equals(event.getEventType()))
                .collect(Collectors.toList());
    }

    private void saveOrder(OrderState state) {
        orderRepository.save(Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(state)
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryStoreCode("STORE001")
                .deliveryVin("VIN001")
                .build());
    }

    private String saveBookedAppointment() {
        String appointmentNo = "DA" + orderNo;
        deliveryRepository.saveAppointment(DeliveryAppointment.builder()
                .appointmentId("DAID-" + appointmentNo)
                .appointmentNo(appointmentNo)
                .orderId(orderId)
                .appointmentStatus(DeliveryAppointmentStatus.BOOKED)
                .deliveryMode(DeliveryAppointment.MODE_DELIVERY_CENTER)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .appointmentStoreCode("STORE001")
                .appointmentPlace("A")
                .build());
        return appointmentNo;
    }
}
