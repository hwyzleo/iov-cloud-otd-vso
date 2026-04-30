package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderAmountPo;

import java.util.Optional;

/**
 * 订单金额仓储接口
 *
 * @author VSO Team
 */
public interface OrderAmountRepository {

    /**
     * 保存订单金额
     *
     * @param orderAmountPo 订单金额
     * @return 保存后的订单金额
     */
    OrderAmountPo save(OrderAmountPo orderAmountPo);

    /**
     * 根据订单业务 ID 查询金额
     *
     * @param orderId 订单业务 ID
     * @return 订单金额
     */
    Optional<OrderAmountPo> findByOrderId(String orderId);

    /**
     * 删除订单金额（软删除）
     *
     * @param orderId 订单业务 ID
     */
    void delete(String orderId);

}
