package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 配车仓储实现
 */
@Repository
@RequiredArgsConstructor
public class VehicleAssignmentRepositoryImpl implements VehicleAssignmentRepository {

    private final VehicleAssignmentMapper vehicleAssignmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleAssignmentPo save(VehicleAssignmentPo vehicleAssignmentPo) {
        if (vehicleAssignmentPo.getId() == null) {
            vehicleAssignmentMapper.insert(vehicleAssignmentPo);
        } else {
            vehicleAssignmentMapper.updateById(vehicleAssignmentPo);
        }
        return vehicleAssignmentPo;
    }

    @Override
    public Optional<VehicleAssignmentPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<VehicleAssignmentPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleAssignmentPo::getOrderId, orderId)
               .eq(VehicleAssignmentPo::getRowValid, 1)
               .orderByDesc(VehicleAssignmentPo::getAssignTime);
        return Optional.ofNullable(vehicleAssignmentMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String vehicleAssignmentId) {
        VehicleAssignmentPo vehicleAssignmentPo = vehicleAssignmentMapper.selectById(vehicleAssignmentId);
        if (vehicleAssignmentPo != null) {
            vehicleAssignmentPo.setRowValid(0);
            vehicleAssignmentMapper.updateById(vehicleAssignmentPo);
        }
    }

}
