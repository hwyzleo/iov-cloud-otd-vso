package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignDeliveryPersonCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 交付人员分配时间线留痕集成测试（OTD-VSO-DSN-CR-010）
 * <p>
 * 真实本地库事务内验证：首次分配 / 改派 / 幂等留痕，以及 MPT 可见、C 端过滤。
 * 本测试不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("交付人员分配留痕 - 集成测试")
class OrderAssignDeliveryPersonTimelineIntegrationTest {

    private static final String EVENT_TYPE = OrderTimelineEventType.DELIVERY_PERSON_ASSIGNED.getCode();

    @Autowired
    private OrderAppService orderAppService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderTimelineAppService orderTimelineAppService;

    @MockBean
    private ConfigurationService configurationService;

    private String orderId;
    private String orderNo;

    @BeforeEach
    void setUp() {
        orderId = "ORD_TL_" + System.nanoTime();
        orderNo = "VSO_TL_" + System.nanoTime();
        orderRepository.save(Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(OrderState.PREPARE_DELIVER)
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .build());
    }

    private void assign(String deliveryPersonId, String deliveryPersonName, String operatorId) {
        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(orderNo)
                .deliveryPersonId(deliveryPersonId)
                .deliveryPersonName(deliveryPersonName)
                .operatorId(operatorId)
                .build());
    }

    private List<OrderTimelineResult> mptTimeline() {
        return orderAppService.getTimelineByOrderId(orderId);
    }

    private List<OrderTimelineResult> customerTimeline() {
        return orderTimelineAppService.customerTimelineOf(orderId);
    }

    @Test
    @DisplayName("首次分配写入一条 MPT 可见、C 端不可见的时间线事件")
    void 首次分配写入时间线() {
        assign("1001", "张三", "admin");

        List<OrderTimelineResult> timeline = mptTimeline();
        assertEquals(1, timeline.size());
        OrderTimelineResult event = timeline.get(0);
        assertEquals(EVENT_TYPE, event.getEventType());
        assertEquals("分配交付人员", event.getEventName());
        assertEquals("admin", event.getOperatorId());
        assertEquals("mpt_operator", event.getOperatorRole());
        assertEquals("mpt", event.getOperateSource());
        assertEquals(String.valueOf(OrderState.PREPARE_DELIVER.getValue()), event.getBeforeStatus());
        assertEquals(event.getBeforeStatus(), event.getAfterStatus(), "分配交付人员不得变更订单状态");
        assertTrue(event.getEventRemark().contains("张三") && event.getEventRemark().contains("1001"));

        assertTrue(customerTimeline().isEmpty(), "C 端时间线必须过滤交付人员分配事件");
        assertEquals("1001", orderRepository.findByOrderId(orderId).orElseThrow().getDeliveryPersonId());
    }

    @Test
    @DisplayName("改派追加第二条事件，备注保留原交付人员与新交付人员")
    void 改派追加事件() {
        assign("1001", "张三", "admin");
        assign("1002", "李四", "operator2");

        List<OrderTimelineResult> timeline = mptTimeline();
        assertEquals(2, timeline.size());
        OrderTimelineResult second = timeline.get(1);
        assertEquals(EVENT_TYPE, second.getEventType());
        assertEquals("operator2", second.getOperatorId());
        assertTrue(second.getEventRemark().contains("李四(1002)"), "备注应包含新交付人员");
        assertTrue(second.getEventRemark().contains("张三(1001)"), "备注应保留原交付人员");
        assertEquals("1002", orderRepository.findByOrderId(orderId).orElseThrow().getDeliveryPersonId());
        assertTrue(customerTimeline().isEmpty(), "C 端时间线不得返回内部事件");
    }

    @Test
    @DisplayName("同一人员重复提交不追加事件（幂等）")
    void 同一人员重复提交不追加事件() {
        assign("1001", "张三", "admin");
        assign("1001", "张三", "admin");

        assertEquals(1, mptTimeline().size(), "重复提交同一交付人员不得重复留痕");
        assertTrue(customerTimeline().isEmpty());
    }

    @Test
    @DisplayName("订单不存在抛 ORDER_NOT_EXIST 且不写事件")
    void 订单不存在不写事件() {
        String missingOrderNo = "VSO_MISSING_" + System.nanoTime();
        assertThrows(OrderNotExistException.class, () -> orderAppService.assignDeliveryPerson(
                AssignDeliveryPersonCmd.builder()
                        .orderNo(missingOrderNo)
                        .deliveryPersonId("1001")
                        .deliveryPersonName("张三")
                        .operatorId("admin")
                        .build()));
    }
}
