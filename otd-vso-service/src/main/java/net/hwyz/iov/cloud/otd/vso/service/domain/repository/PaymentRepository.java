package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 支付仓储接口（领域类型）
 *
 * @author VSO Team
 */
public interface PaymentRepository {

    /**
     * 保存支付单
     */
    Payment save(Payment payment);

    /**
     * 根据支付单号查询（具名 SQL）
     */
    Optional<Payment> findByPaymentNo(String paymentNo);

    /**
     * 查询订单下全部支付单（创建时间倒序）
     */
    List<Payment> findByOrderId(String orderId);

    /**
     * 查询订单同一阶段的活动支付单（PENDING_PAYMENT / PROCESSING）
     */
    Optional<Payment> findActiveByOrderIdAndStage(String orderId, String paymentStage);

    /**
     * 查询订单下全部成功支付单（按支付成功时间正序，用于退款分摊）
     */
    List<Payment> findSuccessfulByOrderId(String orderId);

    /**
     * 批量查询多个订单的成功支付单（MPT 列表用，避免 N+1）
     */
    List<Payment> findSuccessfulByOrderIds(List<String> orderIds);

    /**
     * 条件更新支付状态
     *
     * @return 影响行数，0 表示状态已变化（并发或冲突）
     */
    int updateStatusConditional(String paymentNo, PaymentStatus targetStatus, List<PaymentStatus> expectedStatuses,
                                String externalTradeNo, LocalDateTime payTime, String failReason);

    /**
     * 删除支付单
     */
    void delete(String paymentId);
}
