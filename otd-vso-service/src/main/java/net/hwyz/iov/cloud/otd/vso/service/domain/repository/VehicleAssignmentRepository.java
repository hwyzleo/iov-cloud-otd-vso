package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.VehicleAssignment;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.VehicleAssignmentPo;

import java.util.List;
import java.util.Optional;

/**
 * 配车仓储接口
 */
public interface VehicleAssignmentRepository {

    VehicleAssignmentPo save(VehicleAssignmentPo vehicleAssignmentPo);

    Optional<VehicleAssignmentPo> findByOrderId(String orderId);

    void delete(String vehicleAssignmentId);

    /**
     * 根据订单号查找配车记录
     */
    Optional<VehicleAssignmentPo> findByOrderNo(String orderNo);

    /**
     * 根据VIN查找当前占用记录（ASSIGNED或BOUND状态）
     */
    Optional<VehicleAssignmentPo> findOccupiedByVin(String vin);

    /**
     * 查找所有已过期但未释放的配车记录
     */
    List<VehicleAssignmentPo> findExpiredAssignments();

    /**
     * 保存领域模型
     */
    VehicleAssignment saveDomain(VehicleAssignment assignment);

    /**
     * 根据订单ID查找领域模型
     */
    Optional<VehicleAssignment> findDomainByOrderId(String orderId);
}