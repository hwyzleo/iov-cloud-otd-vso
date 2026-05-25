package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.AssignmentType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.VehicleAssignmentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.VehicleAssignmentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
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

    @Override
    public Optional<VehicleAssignmentPo> findByOrderNo(String orderNo) {
        return Optional.empty();
    }

    @Override
    public Optional<VehicleAssignmentPo> findOccupiedByVin(String vin) {
        return Optional.ofNullable(vehicleAssignmentMapper.selectOccupiedByVin(vin));
    }

    @Override
    public List<VehicleAssignmentPo> findExpiredAssignments() {
        return vehicleAssignmentMapper.selectExpiredAssignments();
    }

    @Override
    public VehicleAssignment saveDomain(VehicleAssignment assignment) {
        VehicleAssignmentPo po = convertToPo(assignment);
        return save(po) != null ? assignment : null;
    }

    @Override
    public Optional<VehicleAssignment> findDomainByOrderId(String orderId) {
        return findByOrderId(orderId).map(this::convertToDomain);
    }

    private VehicleAssignmentPo convertToPo(VehicleAssignment assignment) {
        VehicleAssignmentPo po = new VehicleAssignmentPo();
        po.setVehicleAssignmentId(assignment.getVehicleAssignmentId());
        po.setOrderId(assignment.getOrderId());
        po.setAssignmentType(assignment.getAssignmentType().getCode());
        po.setVin(assignment.getVin());
        po.setVehicleId(assignment.getVehicleId());
        po.setAssignStatus(assignment.getAssignStatus().getCode());
        po.setManualAssignFlag(assignment.getManualAssignFlag());
        po.setManualAssignReason(assignment.getManualAssignReason());
        po.setUnbindReason(assignment.getUnbindReason());
        po.setOccupyExpireTime(assignment.getOccupyExpireTime());
        po.setAssignTime(assignment.getAssignTime());
        po.setBindTime(assignment.getBindTime());
        po.setReleaseTime(assignment.getReleaseTime());
        return po;
    }

    private VehicleAssignment convertToDomain(VehicleAssignmentPo po) {
        return VehicleAssignment.builder()
                .vehicleAssignmentId(po.getVehicleAssignmentId())
                .orderId(po.getOrderId())
                .assignmentType(AssignmentType.fromCode(po.getAssignmentType()))
                .vehicleSourceType(po.getVehicleSourceType())
                .vin(po.getVin())
                .vehicleId(po.getVehicleId())
                .assignStatus(AssignStatus.fromCode(po.getAssignStatus()))
                .manualAssignFlag(po.getManualAssignFlag())
                .manualAssignReason(po.getManualAssignReason())
                .unbindReason(po.getUnbindReason())
                .occupyExpireTime(po.getOccupyExpireTime())
                .assignTime(po.getAssignTime())
                .bindTime(po.getBindTime())
                .releaseTime(po.getReleaseTime())
                .build();
    }
}