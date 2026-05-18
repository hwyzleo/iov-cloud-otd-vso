package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;

import java.util.Optional;

/**
 * 订单车型配置快照仓储接口
 */
public interface OrderVehicleSnapshotRepository {

    OrderVehicleSnapshotPo save(OrderVehicleSnapshotPo orderVehicleSnapshotPo);

    Optional<OrderVehicleSnapshotPo> findByOrderId(String orderId);

    void delete(String snapshotId);

    Integer findMaxVersionByOrderId(String orderId);

}
