package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VehicleAssignmentRepositoryImpl implements VehicleAssignmentRepository {

    private final VehicleAssignmentMapper vehicleAssignmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleAssignmentPo save(VehicleAssignmentPo vehicleAssignmentPo) {
        if (vehicleAssignmentPo.getId() == null) {
            vehicleAssignmentMapper.insertPo(vehicleAssignmentPo);
        } else {
            vehicleAssignmentMapper.updatePo(vehicleAssignmentPo);
        }
        return vehicleAssignmentPo;
    }

    @Override
    public Optional<VehicleAssignmentPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "assignTime DESC");
        return vehicleAssignmentMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String vehicleAssignmentId) {
        VehicleAssignmentPo vehicleAssignmentPo = vehicleAssignmentMapper.selectPoById(Long.valueOf(vehicleAssignmentId));
        if (vehicleAssignmentPo != null) {
            vehicleAssignmentPo.setRowValid(0);
            vehicleAssignmentMapper.updatePo(vehicleAssignmentPo);
        }
    }

}