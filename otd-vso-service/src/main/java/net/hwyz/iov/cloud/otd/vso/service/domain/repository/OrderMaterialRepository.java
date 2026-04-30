package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderMaterialPo;

import java.util.Optional;

/**
 * 订单资料仓储接口
 */
public interface OrderMaterialRepository {

    OrderMaterialPo save(OrderMaterialPo orderMaterialPo);

    Optional<OrderMaterialPo> findByOrderIdAndType(String orderId, String materialType);

    void delete(String materialId);

}
