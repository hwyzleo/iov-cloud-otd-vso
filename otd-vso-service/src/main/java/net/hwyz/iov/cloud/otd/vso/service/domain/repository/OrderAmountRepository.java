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
     * 批量查询多个订单的金额（MPT 列表用，避免 N+1）
     *
     * @param orderIds 订单业务 ID 集合
     * @return 订单业务 ID -> 订单金额 PO
     */
    java.util.Map<String, OrderAmountPo> findPoByOrderIds(java.util.List<String> orderIds);

    /**
     * 根据订单业务 ID 查询领域金额对象
     *
     * @param orderId 订单业务 ID
     * @return 订单金额领域对象
     */
    Optional<net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount> findDomainByOrderId(String orderId);

    /**
     * 删除订单金额（软删除）
     *
     * @param orderId 订单业务 ID
     */
    void delete(String orderId);

}
