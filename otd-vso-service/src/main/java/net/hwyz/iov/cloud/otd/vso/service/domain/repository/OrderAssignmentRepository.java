package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAssignmentPo;

import java.util.Optional;

/**
 * 订单归属仓储接口
 */
public interface OrderAssignmentRepository {

    OrderAssignmentPo save(OrderAssignmentPo orderAssignmentPo);

    Optional<OrderAssignmentPo> findByOrderId(String orderId);

    void delete(String assignmentId);

}
