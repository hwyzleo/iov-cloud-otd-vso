package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;

import java.util.Optional;

/**
 * 交付仓储接口
 */
public interface DeliveryRepository {

    DeliveryAppointmentPo saveAppointment(DeliveryAppointmentPo appointmentPo);

    Optional<DeliveryAppointmentPo> findByAppointmentNo(String appointmentNo);

    DeliveryRecordPo saveRecord(DeliveryRecordPo recordPo);

    Optional<DeliveryRecordPo> findByOrderId(String orderId);

}
