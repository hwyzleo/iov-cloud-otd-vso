package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryAppointmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryRecordMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryAppointmentMapper deliveryAppointmentMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAppointmentPo saveAppointment(DeliveryAppointmentPo appointmentPo) {
        if (appointmentPo.getId() == null) {
            deliveryAppointmentMapper.insertPo(appointmentPo);
        } else {
            deliveryAppointmentMapper.updatePo(appointmentPo);
        }
        return appointmentPo;
    }

    @Override
    public Optional<DeliveryAppointmentPo> findByAppointmentNo(String appointmentNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("deliveryAppointmentNo", appointmentNo);
        params.put("rowValid", 1);
        return deliveryAppointmentMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryRecordPo saveRecord(DeliveryRecordPo recordPo) {
        if (recordPo.getId() == null) {
            deliveryRecordMapper.insertPo(recordPo);
        } else {
            deliveryRecordMapper.updatePo(recordPo);
        }
        return recordPo;
    }

    @Override
    public Optional<DeliveryRecordPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return deliveryRecordMapper.selectPoByMap(params).stream().findFirst();
    }

}