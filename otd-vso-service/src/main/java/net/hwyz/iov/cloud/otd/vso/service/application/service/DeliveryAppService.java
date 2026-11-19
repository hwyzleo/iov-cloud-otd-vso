package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DeliveryAppointmentStatusNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointmentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 交付应用服务
 * <p>
 * US-009 / INV-004：唯一 booked 预约（订单锁保证）、改期/取消归属校验、
 * 交付时创建交付记录并完成预约；OTD-VSO-DSN-CR-016 起激活退出订单生命周期，交付后由 MPT 人工办结。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAppService {

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final OrderLockService orderLockService;
    private final OrderTimelineAppService orderTimelineAppService;

    /**
     * 创建交付预约（仅 PREPARE_DELIVER，且无有效预约）
     */
    public DeliveryAppointmentResult create(String accountId, String orderNo, LocalDateTime appointmentTime,
                                            String storeCode, String place) {
        return orderLockService.executeWithLock(orderNo, accountId, "deliveryAppointment", () -> {
            Order order = orderRepository.findByOrderNoAndAccountId(orderNo, accountId)
                    .orElseThrow(() -> new OrderNotExistException(orderNo));
            // OTD-VSO-DSN-CR-014：600/620 均可预约，避免支付尾款后失去预约入口
            if (order.getOrderState() != OrderState.PREPARE_DELIVER
                    && order.getOrderState() != OrderState.FINAL_PAYMENT_PAID) {
                throw new OrderStateNotAllowedException(
                        "车辆销售订单[" + orderNo + "]当前状态[" + order.getOrderState() + "]不允许创建交付预约");
            }
            if (deliveryRepository.findBookedByOrderId(order.getId()).isPresent()) {
                throw new DeliveryAppointmentConflictException("订单[" + orderNo + "]已存在有效交付预约");
            }
            DeliveryAppointment appointment = DeliveryAppointment.builder()
                    .appointmentId(IdUtil.nanoId(15))
                    .appointmentNo("DA" + IdUtil.nanoId(12))
                    .orderId(order.getId())
                    .appointmentStatus(DeliveryAppointmentStatus.BOOKED)
                    .deliveryMode(DeliveryAppointment.MODE_DELIVERY_CENTER)
                    .appointmentTime(appointmentTime)
                    .appointmentStoreCode(storeCode)
                    .appointmentPlace(place)
                    .operatorId(accountId)
                    .build();
            deliveryRepository.saveAppointment(appointment);
            orderTimelineAppService.record(order.getId(), OrderTimelineEventType.DELIVERY_APPOINTMENT_CREATED,
                    String.valueOf(order.getOrderState().getValue()), String.valueOf(order.getOrderState().getValue()),
                    accountId, "order_user", "capp", appointment.getAppointmentNo(), null,
                    "预约时间：" + appointmentTime + "，交付中心：" + storeCode);
            return toResult(order, appointment);
        });
    }

    /**
     * 修改预约（本人 booked）
     */
    public DeliveryAppointmentResult modify(String accountId, String appointmentNo, LocalDateTime appointmentTime,
                                            String storeCode, String place, String reason) {
        DeliveryAppointment appointment = deliveryRepository.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new DeliveryAppointmentNotExistException(appointmentNo));
        Order order = orderRepository.findByOrderIdAndAccountId(appointment.getOrderId(), accountId)
                .orElseThrow(() -> new OrderNotExistException(appointmentNo));
        if (appointment.getAppointmentStatus() != DeliveryAppointmentStatus.BOOKED) {
            throw new DeliveryAppointmentStatusNotAllowedException(
                    "预约[" + appointmentNo + "]当前状态[" + appointment.getAppointmentStatus() + "]不允许修改");
        }
        appointment.reschedule(appointmentTime, storeCode, place, reason);
        deliveryRepository.saveAppointment(appointment);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.DELIVERY_APPOINTMENT_RESCHEDULED,
                String.valueOf(order.getOrderState().getValue()), String.valueOf(order.getOrderState().getValue()),
                accountId, "order_user", "capp", appointmentNo, null,
                "改期原因：" + reason + "，新预约时间：" + appointmentTime);
        return toResult(order, appointment);
    }

    /**
     * 取消预约（本人 booked）
     */
    public DeliveryAppointmentResult cancel(String accountId, String appointmentNo, String reason) {
        DeliveryAppointment appointment = deliveryRepository.findByAppointmentNo(appointmentNo)
                .orElseThrow(() -> new DeliveryAppointmentNotExistException(appointmentNo));
        Order order = orderRepository.findByOrderIdAndAccountId(appointment.getOrderId(), accountId)
                .orElseThrow(() -> new OrderNotExistException(appointmentNo));
        appointment.cancel(reason);
        deliveryRepository.saveAppointment(appointment);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.DELIVERY_APPOINTMENT_CANCELLED,
                String.valueOf(order.getOrderState().getValue()), String.valueOf(order.getOrderState().getValue()),
                accountId, "order_user", "capp", appointmentNo, null, "取消原因：" + reason);
        return toResult(order, appointment);
    }

    /**
     * 查询订单交付信息（当前有效预约 + 最近交付记录）
     */
    public DeliveryAppointmentResult getByOrderNo(String accountId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndAccountId(orderNo, accountId)
                .orElseThrow(() -> new OrderNotExistException(orderNo));
        DeliveryAppointment appointment = deliveryRepository.findLatestByOrderId(order.getId()).orElse(null);
        return toResult(order, appointment);
    }

    /**
     * 运营（MPT）按业务订单 ID 查询交付信息（只读，不做账号隔离）
     * <p>
     * 未发生预约时返回 null，不得合成预约单号。
     */
    public DeliveryAppointmentResult getByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        DeliveryAppointment appointment = deliveryRepository.findLatestByOrderId(order.getId()).orElse(null);
        if (appointment == null) {
            return null;
        }
        return toResult(order, appointment);
    }

    /**
     * 交付完成：创建交付记录并完成有效预约（与订单交付同一事务）
     * <p>
     * OTD-VSO-DSN-CR-015：操作人/操作角色/操作来源由调用入口显式传入，不再硬编码 delivery_service；
     * 实际交付时间取服务端时间，备注可选。
     * OTD-VSO-DSN-CR-016：返回不可变交付记录，供调用方同事务写入交付事件，不得由调用方重新查询或伪造。
     *
     * @param order          订单（交付前真实状态）
     * @param operatorId     操作人 ID
     * @param operatorRole   操作角色
     * @param operateSource  操作来源
     * @param deliveryRemark 交付备注（可选）
     * @return 已持久化的交付记录
     */
    @Transactional(rollbackFor = Exception.class)
    public DeliveryRecord completeDelivery(Order order, String operatorId, String operatorRole,
                                           String operateSource, String deliveryRemark) {
        DeliveryAppointment appointment = deliveryRepository.findBookedByOrderId(order.getId()).orElse(null);
        if (appointment != null) {
            appointment.complete();
            deliveryRepository.saveAppointment(appointment);
        }
        DeliveryRecord record = DeliveryRecord.builder()
                .deliveryRecordId(IdUtil.nanoId(15))
                .orderId(order.getId())
                .deliveryAppointmentId(appointment == null ? null : appointment.getAppointmentId())
                .deliveryStatus("delivered")
                .deliveryMode(DeliveryAppointment.MODE_DELIVERY_CENTER)
                .actualDeliveryTime(LocalDateTime.now())
                .deliveryPlace(appointment == null ? null : appointment.getAppointmentPlace())
                .deliveryStoreCode(order.getDeliveryStoreCode())
                .vehicleVin(order.getDeliveryVin())
                .deliveryRemark(StrUtil.blankToDefault(deliveryRemark, "外部交付服务确认已交付"))
                .build();
        deliveryRepository.saveRecord(record);
        // 时间线使用订单真实前状态（OTD-VSO-DSN-CR-014：620 → 650，不得硬编码 600）；
        // ORDER_DELIVERED 在本方法单点写入，MPT 操作人/角色/来源必须真实留痕
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.ORDER_DELIVERED,
                String.valueOf(order.getOrderState().getValue()),
                String.valueOf(OrderState.DELIVERED.getValue()),
                operatorId, operatorRole, operateSource, record.getDeliveryRecordId(), null,
                "交付完成，VIN：" + record.getVehicleVin());
        return record;
    }

    private DeliveryAppointmentResult toResult(Order order, DeliveryAppointment appointment) {
        List<DeliveryAppointmentResult.RecordItem> records = new ArrayList<>();
        deliveryRepository.findRecordByOrderId(order.getId()).ifPresent(record ->
                records.add(DeliveryAppointmentResult.RecordItem.builder()
                        .deliveryRecordId(record.getDeliveryRecordId())
                        .deliveryStatus(record.getDeliveryStatus())
                        .actualDeliveryTime(record.getActualDeliveryTime())
                        .deliveryPlace(record.getDeliveryPlace())
                        .vehicleVin(record.getVehicleVin())
                        .build()));
        return DeliveryAppointmentResult.builder()
                .appointmentNo(appointment == null ? null : appointment.getAppointmentNo())
                .orderNo(order.getOrderNo())
                .appointmentStatus(appointment == null || appointment.getAppointmentStatus() == null
                        ? null : appointment.getAppointmentStatus().getValue())
                .deliveryMode(appointment == null ? null : appointment.getDeliveryMode())
                .appointmentTime(appointment == null ? null : appointment.getAppointmentTime())
                .appointmentStoreCode(appointment == null ? null : appointment.getAppointmentStoreCode())
                .appointmentPlace(appointment == null ? null : appointment.getAppointmentPlace())
                .deliveryPersonId(order.getDeliveryPersonId())
                .deliveryPersonName(order.getDeliveryPersonName())
                .vehicleVin(order.getDeliveryVin())
                .delivered(order.getOrderState() == OrderState.DELIVERED
                        || order.getOrderState() == OrderState.COMPLETED)
                .records(records)
                .build();
    }
}
