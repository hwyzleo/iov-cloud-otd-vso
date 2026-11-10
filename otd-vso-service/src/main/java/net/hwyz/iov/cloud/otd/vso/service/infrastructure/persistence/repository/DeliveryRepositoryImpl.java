package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryAppointment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.DeliveryRecord;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.DeliveryPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryAppointmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryRecordMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 交付仓储实现（领域类型）
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryAppointmentMapper deliveryAppointmentMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAppointment saveAppointment(DeliveryAppointment appointment) {
        DeliveryAppointmentPo existing = deliveryAppointmentMapper.selectByAppointmentNo(appointment.getAppointmentNo());
        DeliveryAppointmentPo po = DeliveryPoConverter.fromDomain(appointment,
                existing == null ? null : existing.getId());
        if (existing == null) {
            deliveryAppointmentMapper.insertPo(po);
        } else {
            deliveryAppointmentMapper.updatePo(po);
        }
        return appointment;
    }

    @Override
    public Optional<DeliveryAppointment> findByAppointmentNo(String appointmentNo) {
        return Optional.ofNullable(deliveryAppointmentMapper.selectByAppointmentNo(appointmentNo))
                .map(DeliveryPoConverter::toDomain);
    }

    @Override
    public Optional<DeliveryAppointment> findBookedByOrderId(String orderId) {
        return Optional.ofNullable(deliveryAppointmentMapper.selectBookedByOrderId(orderId))
                .map(DeliveryPoConverter::toDomain);
    }

    @Override
    public Optional<DeliveryAppointment> findLatestByOrderId(String orderId) {
        return Optional.ofNullable(deliveryAppointmentMapper.selectLatestByOrderId(orderId))
                .map(DeliveryPoConverter::toDomain);
    }

    @Override
    public List<DeliveryAppointment> findBookedByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return deliveryAppointmentMapper.selectBookedByOrderIds(orderIds).stream()
                .map(DeliveryPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryRecord saveRecord(DeliveryRecord record) {
        DeliveryRecordPo po = DeliveryPoConverter.fromDomain(record);
        DeliveryRecordPo existing = deliveryRecordMapper.selectByOrderId(record.getOrderId());
        if (existing == null) {
            deliveryRecordMapper.insertPo(po);
        } else {
            po.setId(existing.getId());
            deliveryRecordMapper.updatePo(po);
        }
        return record;
    }

    @Override
    public Optional<DeliveryRecord> findRecordByOrderId(String orderId) {
        return Optional.ofNullable(DeliveryPoConverter.toDomain(deliveryRecordMapper.selectByOrderId(orderId)));
    }
}
