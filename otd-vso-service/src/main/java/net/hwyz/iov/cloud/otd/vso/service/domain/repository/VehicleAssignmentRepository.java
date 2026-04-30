package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;

import java.util.Optional;

/**
 * 配车仓储接口
 */
public interface VehicleAssignmentRepository {

    VehicleAssignmentPo save(VehicleAssignmentPo vehicleAssignmentPo);

    Optional<VehicleAssignmentPo> findByOrderId(String orderId);

    void delete(String vehicleAssignmentId);

}
