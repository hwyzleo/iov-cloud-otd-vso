package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderStatusDimensionPo;

import java.util.Optional;

/**
 * 订单维度状态仓储接口
 */
public interface OrderStatusDimensionRepository {

    OrderStatusDimensionPo save(OrderStatusDimensionPo orderStatusDimensionPo);

    Optional<OrderStatusDimensionPo> findByOrderId(String orderId);

    void delete(String statusDimensionId);

}
