package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPartyPo;

import java.util.List;
import java.util.Optional;

/**
 * 订单客户仓储接口
 */
public interface OrderPartyRepository {

    OrderPartyPo save(OrderPartyPo orderPartyPo);

    Optional<OrderPartyPo> findByOrderIdAndRole(String orderId, String partyRole);

    List<OrderPartyPo> findByOrderId(String orderId);

    void delete(String partyId);

}
