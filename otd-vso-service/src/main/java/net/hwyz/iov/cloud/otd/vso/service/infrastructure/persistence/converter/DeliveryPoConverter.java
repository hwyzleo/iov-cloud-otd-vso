package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointmentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimeline;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;

/**
 * 交付与时间线对象转换器（Domain ↔ PO）
 *
 * @author VSO Team
 */
public final class DeliveryPoConverter {

    private DeliveryPoConverter() {
    }

    public static DeliveryAppointment toDomain(DeliveryAppointmentPo po) {
        if (po == null) {
            return null;
        }
        return DeliveryAppointment.builder()
                .appointmentId(po.getDeliveryAppointmentId())
                .appointmentNo(po.getDeliveryAppointmentNo())
                .orderId(po.getOrderId())
                .appointmentStatus(DeliveryAppointmentStatus.fromValue(po.getAppointmentStatus()))
                .deliveryMode(po.getDeliveryMode())
                .appointmentTime(po.getAppointmentTime())
                .appointmentPlace(po.getAppointmentPlace())
                .appointmentStoreCode(po.getAppointmentStoreCode())
                .rescheduleReason(po.getRescheduleReason())
                .cancelReason(po.getCancelReason())
                .operatorId(po.getOperatorId())
                .build();
    }

    public static DeliveryAppointmentPo fromDomain(DeliveryAppointment appointment, Long id) {
        DeliveryAppointmentPo po = new DeliveryAppointmentPo();
        po.setId(id);
        po.setDeliveryAppointmentId(appointment.getAppointmentId());
        po.setDeliveryAppointmentNo(appointment.getAppointmentNo());
        po.setOrderId(appointment.getOrderId());
        po.setAppointmentStatus(appointment.getAppointmentStatus() == null ? null : appointment.getAppointmentStatus().getValue());
        po.setDeliveryMode(appointment.getDeliveryMode());
        po.setAppointmentTime(appointment.getAppointmentTime());
        po.setAppointmentPlace(appointment.getAppointmentPlace());
        po.setAppointmentStoreCode(appointment.getAppointmentStoreCode());
        po.setRescheduleReason(appointment.getRescheduleReason());
        po.setCancelReason(appointment.getCancelReason());
        po.setOperatorId(appointment.getOperatorId());
        return po;
    }

    public static DeliveryRecord toDomain(DeliveryRecordPo po) {
        if (po == null) {
            return null;
        }
        return DeliveryRecord.builder()
                .deliveryRecordId(po.getDeliveryRecordId())
                .orderId(po.getOrderId())
                .deliveryAppointmentId(po.getDeliveryAppointmentId())
                .deliveryStatus(po.getDeliveryStatus())
                .deliveryMode(po.getDeliveryMode())
                .actualDeliveryTime(po.getActualDeliveryTime())
                .deliveryPlace(po.getDeliveryPlace())
                .deliveryStoreCode(po.getDeliveryStoreCode())
                .vehicleVin(po.getVehicleVin())
                .deliveryRemark(po.getDeliveryRemark())
                .build();
    }

    public static DeliveryRecordPo fromDomain(DeliveryRecord record) {
        DeliveryRecordPo po = new DeliveryRecordPo();
        po.setDeliveryRecordId(record.getDeliveryRecordId());
        po.setOrderId(record.getOrderId());
        po.setDeliveryAppointmentId(record.getDeliveryAppointmentId());
        po.setDeliveryStatus(record.getDeliveryStatus());
        po.setDeliveryMode(record.getDeliveryMode());
        po.setActualDeliveryTime(record.getActualDeliveryTime());
        po.setDeliveryPlace(record.getDeliveryPlace());
        po.setDeliveryStoreCode(record.getDeliveryStoreCode());
        po.setVehicleVin(record.getVehicleVin());
        po.setDeliveryRemark(record.getDeliveryRemark());
        po.setReceiverIsBuyer(1);
        po.setAuthorizedFlag(0);
        return po;
    }

    public static OrderTimelinePo fromDomain(OrderTimeline timeline) {
        OrderTimelinePo po = new OrderTimelinePo();
        po.setTimelineId(timeline.getTimelineId());
        po.setOrderId(timeline.getOrderId());
        po.setEventType(timeline.getEventType());
        po.setEventName(timeline.getEventName());
        po.setBeforeStatus(timeline.getBeforeStatus());
        po.setAfterStatus(timeline.getAfterStatus());
        po.setOperatorId(timeline.getOperatorId());
        po.setOperatorRole(timeline.getOperatorRole());
        po.setOperateSource(timeline.getOperateSource());
        po.setRelatedDocNo(timeline.getRelatedDocNo());
        po.setResult(timeline.getResult());
        po.setFailReason(timeline.getFailReason());
        po.setEventRemark(timeline.getEventRemark());
        po.setEventTime(timeline.getEventTime());
        return po;
    }

    public static OrderTimeline toDomain(OrderTimelinePo po) {
        if (po == null) {
            return null;
        }
        return OrderTimeline.builder()
                .timelineId(po.getTimelineId())
                .orderId(po.getOrderId())
                .eventType(po.getEventType())
                .eventName(po.getEventName())
                .beforeStatus(po.getBeforeStatus())
                .afterStatus(po.getAfterStatus())
                .operatorId(po.getOperatorId())
                .operatorRole(po.getOperatorRole())
                .operateSource(po.getOperateSource())
                .relatedDocNo(po.getRelatedDocNo())
                .result(po.getResult())
                .failReason(po.getFailReason())
                .eventRemark(po.getEventRemark())
                .eventTime(po.getEventTime())
                .build();
    }
}
