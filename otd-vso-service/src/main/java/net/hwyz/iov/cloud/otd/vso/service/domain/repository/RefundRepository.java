package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;

import java.util.List;
import java.util.Optional;

/**
 * 退款主单仓储接口（领域类型）
 *
 * @author VSO Team
 */
public interface RefundRepository {

    /**
     * 保存退款主单及明细
     */
    Refund save(Refund refund);

    /**
     * 根据退款单号查询（含明细）
     */
    Optional<Refund> findByRefundNo(String refundNo);

    /**
     * 根据退款业务 ID 查询（含明细）
     */
    Optional<Refund> findByRefundId(String refundId);

    /**
     * 查询订单下未完结或最近一次的退款主单（pending/processing/failed）
     */
    Optional<Refund> findActiveByOrderId(String orderId);

    /**
     * 查询订单下全部退款主单
     */
    List<Refund> findByOrderId(String orderId);

    /**
     * 更新主单状态
     */
    void updateStatus(Refund refund);
}
