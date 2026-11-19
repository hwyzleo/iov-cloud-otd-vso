package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointmentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderOutboxRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;

/**
 * 确认交付事务回滚集成测试（OTD-VSO-DSN-CR-015）
 * <p>
 * 预约完成、交付记录、订单状态与时间线必须在同一事务内；时间线写入异常时整体回滚。
 * 本用例不使用测试级事务包裹，以验证真实提交/回滚边界，结束后清理测试数据。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@DisplayName("确认交付 - 事务回滚集成测试")
class OrderAppServiceDeliveredRollbackIntegrationTest {

    @Autowired
    private OrderAppService orderAppService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private OrderOutboxRepository orderOutboxRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private ConfigurationService configurationService;
    @SpyBean
    private OrderTimelineAppService orderTimelineAppService;

    @AfterEach
    void cleanup() {
        // 明确清理，避免无测试事务时污染本地库
        for (String table : new String[]{"vso_order_timeline", "vso_delivery_record",
                "vso_delivery_appointment", "vso_timeout_task", "vso_outbox", "vso_order"}) {
            try {
                if ("vso_outbox".equals(table)) {
                    jdbcTemplate.update("DELETE FROM " + table + " WHERE aggregate_id = ?", "ORD_DLV_RB_" + testSuffix);
                } else {
                    jdbcTemplate.update("DELETE FROM " + table + " WHERE order_id = ?", "ORD_DLV_RB_" + testSuffix);
                }
            } catch (Exception ignored) {
                // 部分表可能不存在于精简测试库，清理不应掩盖用例结果
            }
        }
    }

    private String testSuffix;

    @BeforeEach
    void prepareSuffix() {
        testSuffix = String.valueOf(System.nanoTime());
    }

    @Test
    @DisplayName("时间线写入异常时预约、交付记录、订单状态整体回滚")
    void 时间线异常整体回滚() {
        String orderId = "ORD_DLV_RB_" + testSuffix;
        String orderNo = "VSO_DLV_RB_" + testSuffix;
        orderRepository.save(Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.FORMAL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("OPENIOV")
                .orderState(OrderState.FINAL_PAYMENT_PAID)
                .hasException(false)
                .currentVersionNo(1)
                .lockedFlag(false)
                .reopenFlag(false)
                .deliveryStoreCode("STORE001")
                .deliveryVin("VIN001")
                .build());
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

        doThrow(new IllegalStateException("模拟时间线写入失败"))
                .when(orderTimelineAppService).record(anyString(), any(OrderTimelineEventType.class),
                        anyString(), anyString(), anyString(), anyString(), anyString(),
                        any(), any(), anyString());

        assertThrows(IllegalStateException.class, () -> orderAppService.delivered(DeliveredCmd.builder()
                .orderId(orderId)
                .operatorId("mpt-user")
                .operatorRole("mpt_operator")
                .operateSource("mpt")
                .deliveryRemark("回滚验证")
                .build()));

        assertEquals(OrderState.FINAL_PAYMENT_PAID,
                orderRepository.findByOrderId(orderId).orElseThrow().getOrderState(),
                "订单状态不得部分提交");
        assertEquals(DeliveryAppointmentStatus.BOOKED,
                deliveryRepository.findByAppointmentNo(appointmentNo).orElseThrow().getAppointmentStatus(),
                "预约完成必须随事务回滚");
        assertTrue(deliveryRepository.findRecordByOrderId(orderId).isEmpty(),
                "交付记录必须随事务回滚");
        assertTrue(orderAppService.getTimelineByOrderId(orderId).isEmpty(),
                "时间线必须随事务回滚");
        assertTrue(!orderOutboxRepository.existsByEventTypeAndAggregateId("vso.order.delivered", orderId),
                "交付事件必须随事务回滚");
    }
}
