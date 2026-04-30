package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.DeliveryRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryAppointmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.DeliveryRecordMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryAppointmentPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.DeliveryRecordPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 交付仓储实现
 */
@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryAppointmentMapper deliveryAppointmentMapper;
    private final DeliveryRecordMapper deliveryRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryAppointmentPo saveAppointment(DeliveryAppointmentPo appointmentPo) {
        if (appointmentPo.getId() == null) {
            deliveryAppointmentMapper.insert(appointmentPo);
        } else {
            deliveryAppointmentMapper.updateById(appointmentPo);
        }
        return appointmentPo;
    }

    @Override
    public Optional<DeliveryAppointmentPo> findByAppointmentNo(String appointmentNo) {
        LambdaQueryWrapper<DeliveryAppointmentPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryAppointmentPo::getDeliveryAppointmentNo, appointmentNo)
               .eq(DeliveryAppointmentPo::getRowValid, 1);
        return Optional.ofNullable(deliveryAppointmentMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeliveryRecordPo saveRecord(DeliveryRecordPo recordPo) {
        if (recordPo.getId() == null) {
            deliveryRecordMapper.insert(recordPo);
        } else {
            deliveryRecordMapper.updateById(recordPo);
        }
        return recordPo;
    }

    @Override
    public Optional<DeliveryRecordPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<DeliveryRecordPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DeliveryRecordPo::getOrderId, orderId)
               .eq(DeliveryRecordPo::getRowValid, 1);
        return Optional.ofNullable(deliveryRecordMapper.selectOne(wrapper));
    }

}
