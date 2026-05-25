package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 改配补款仓储接口
 */
public interface SupplementaryPaymentRepository {

    /**
     * 保存补款记录
     */
    void save(SupplementaryPaymentPo po);

    /**
     * 根据补款单号查询
     */
    Optional<SupplementaryPaymentPo> findBySupplementaryNo(String supplementaryNo);

    /**
     * 根据订单ID和状态查询
     */
    List<SupplementaryPaymentPo> findByOrderIdAndStatus(String orderId, SupplementaryPaymentStatus status);

    /**
     * 根据订单ID查询所有补款记录
     */
    List<SupplementaryPaymentPo> findByOrderId(String orderId);

    /**
     * 查询过期的待支付补款任务
     */
    List<SupplementaryPaymentPo> findExpiredPendingTasks(LocalDateTime expireTime);

    /**
     * 根据支付单号查询补款记录
     */
    Optional<SupplementaryPaymentPo> findByPaymentId(String paymentId);

    /**
     * 更新补款状态
     */
    void updateStatus(String supplementaryNo, SupplementaryPaymentStatus status, String paymentId);
}
