package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentStatusNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointmentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 交付预约测试（US-009 / §14：唯一有效预约、改期、取消、交付完成）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交付预约 - 唯一有效预约/改期/取消/交付完成")
class DeliveryAppServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderLockService orderLockService;
    @Mock
    private OrderTimelineAppService orderTimelineAppService;

    @InjectMocks
    private DeliveryAppService deliveryAppService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .id("OID1")
                .orderNo("SO001")
                .orderType(OrderType.FORMAL)
                .orderState(OrderState.PREPARE_DELIVER)
                .deliveryStoreCode("STORE001")
                .deliveryVin("VIN001")
                .build();
        lenient().when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));
        lenient().when(orderRepository.findByOrderIdAndAccountId("OID1", "ACC1")).thenReturn(Optional.of(order));
        lenient().when(orderLockService.executeWithLock(anyString(), anyString(), anyString(), any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(3)).get());
    }

    @Test
    @DisplayName("PREPARE_DELIVER 创建唯一 booked 预约")
    void 创建预约() {
        when(deliveryRepository.findBookedByOrderId("OID1")).thenReturn(Optional.empty());
        when(deliveryRepository.findRecordByOrderId("OID1")).thenReturn(Optional.empty());

        DeliveryAppointmentResult result = deliveryAppService.create("ACC1", "SO001",
                LocalDateTime.now().plusDays(2), "STORE001", "交付中心 A");

        assertEquals("booked", result.getAppointmentStatus());
        assertEquals(DeliveryAppointment.MODE_DELIVERY_CENTER, result.getDeliveryMode());
        verify(deliveryRepository).saveAppointment(any(DeliveryAppointment.class));
    }

    @Test
    @DisplayName("非 PREPARE_DELIVER 状态：拒绝创建")
    void 非法状态拒绝创建() {
        order = Order.builder()
                .id("OID1").orderNo("SO001").orderType(OrderType.FORMAL)
                .orderState(OrderState.TRANSPORTING).build();
        when(orderRepository.findByOrderNoAndAccountId("SO001", "ACC1")).thenReturn(Optional.of(order));

        assertThrows(OrderStateNotAllowedException.class, () ->
                deliveryAppService.create("ACC1", "SO001", LocalDateTime.now().plusDays(1), "STORE001", "A"));
    }

    @Test
    @DisplayName("已存在 booked 预约：拒绝重复创建")
    void 重复预约冲突() {
        when(deliveryRepository.findBookedByOrderId("OID1")).thenReturn(Optional.of(appointment("DA1",
                DeliveryAppointmentStatus.BOOKED)));

        assertThrows(DeliveryAppointmentConflictException.class, () ->
                deliveryAppService.create("ACC1", "SO001", LocalDateTime.now().plusDays(1), "STORE001", "A"));
    }

    @Test
    @DisplayName("改期：更新预约时间与门店")
    void 改期() {
        DeliveryAppointment appointment = appointment("DA1", DeliveryAppointmentStatus.BOOKED);
        when(deliveryRepository.findByAppointmentNo("DA1")).thenReturn(Optional.of(appointment));
        when(deliveryRepository.findRecordByOrderId("OID1")).thenReturn(Optional.empty());
        LocalDateTime newTime = LocalDateTime.now().plusDays(5);

        DeliveryAppointmentResult result = deliveryAppService.modify("ACC1", "DA1", newTime, "STORE002", "B", "客户改期");

        assertEquals(newTime, result.getAppointmentTime());
        assertEquals("STORE002", result.getAppointmentStoreCode());
        assertEquals("客户改期", appointment.getRescheduleReason());
    }

    @Test
    @DisplayName("取消预约：booked → cancelled")
    void 取消() {
        DeliveryAppointment appointment = appointment("DA1", DeliveryAppointmentStatus.BOOKED);
        when(deliveryRepository.findByAppointmentNo("DA1")).thenReturn(Optional.of(appointment));
        when(deliveryRepository.findRecordByOrderId("OID1")).thenReturn(Optional.empty());

        DeliveryAppointmentResult result = deliveryAppService.cancel("ACC1", "DA1", "行程变化");

        assertEquals("cancelled", result.getAppointmentStatus());
    }

    @Test
    @DisplayName("非 booked 预约不允许取消")
    void 非booked拒绝取消() {
        DeliveryAppointment appointment = appointment("DA1", DeliveryAppointmentStatus.COMPLETED);
        when(deliveryRepository.findByAppointmentNo("DA1")).thenReturn(Optional.of(appointment));

        assertThrows(DeliveryAppointmentStatusNotAllowedException.class, () ->
                deliveryAppService.cancel("ACC1", "DA1", "reason"));
    }

    @Test
    @DisplayName("交付完成：创建交付记录并完成预约")
    void 交付完成() {
        DeliveryAppointment appointment = appointment("DA1", DeliveryAppointmentStatus.BOOKED);
        when(deliveryRepository.findBookedByOrderId("OID1")).thenReturn(Optional.of(appointment));

        deliveryAppService.completeDelivery(order, "delivery_service");

        assertEquals(DeliveryAppointmentStatus.COMPLETED, appointment.getAppointmentStatus());
        ArgumentCaptor<DeliveryRecord> captor = ArgumentCaptor.forClass(DeliveryRecord.class);
        verify(deliveryRepository).saveRecord(captor.capture());
        assertEquals("delivered", captor.getValue().getDeliveryStatus());
        assertEquals("VIN001", captor.getValue().getVehicleVin());
    }

    @Test
    @DisplayName("MPT 按业务订单 ID 查询交付信息（不做账号隔离）")
    void MPT按订单ID查询交付() {
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(deliveryRepository.findLatestByOrderId("OID1"))
                .thenReturn(Optional.of(appointment("DA1", DeliveryAppointmentStatus.BOOKED)));
        when(deliveryRepository.findRecordByOrderId("OID1")).thenReturn(Optional.empty());

        DeliveryAppointmentResult result = deliveryAppService.getByOrderId("OID1");

        assertEquals("DA1", result.getAppointmentNo());
        assertEquals("booked", result.getAppointmentStatus());
        verify(orderRepository, org.mockito.Mockito.never())
                .findByOrderNoAndAccountId(anyString(), anyString());
    }

    @Test
    @DisplayName("MPT 交付查询：未发生预约返回 null，不合成单号")
    void MPT未发生预约返回空() {
        when(orderRepository.findByOrderId("OID1")).thenReturn(Optional.of(order));
        when(deliveryRepository.findLatestByOrderId("OID1")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertNull(deliveryAppService.getByOrderId("OID1"));
    }

    @Test
    @DisplayName("MPT 交付查询：订单不存在返回 ORDER_NOT_EXIST")
    void MPT交付查询订单不存在() {
        when(orderRepository.findByOrderId("MISSING")).thenReturn(Optional.empty());

        assertThrows(net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException.class,
                () -> deliveryAppService.getByOrderId("MISSING"));
    }

    private DeliveryAppointment appointment(String no, DeliveryAppointmentStatus status) {
        return DeliveryAppointment.builder()
                .appointmentId("DAID-" + no)
                .appointmentNo(no)
                .orderId("OID1")
                .appointmentStatus(status)
                .deliveryMode(DeliveryAppointment.MODE_DELIVERY_CENTER)
                .appointmentTime(LocalDateTime.now().plusDays(1))
                .appointmentStoreCode("STORE001")
                .appointmentPlace("A")
                .build();
    }
}
