package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.api.enums.ConfigChangeRefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigChangeRefundPo;

import java.util.List;
import java.util.Optional;

/**
 * 改配退款仓储接口
 */
public interface ConfigChangeRefundRepository {

    /**
     * 保存退款记录
     */
    void save(ConfigChangeRefundPo po);

    /**
     * 根据退款任务单号查询
     */
    Optional<ConfigChangeRefundPo> findByRefundTaskNo(String refundTaskNo);

    /**
     * 根据订单ID和状态查询
     */
    List<ConfigChangeRefundPo> findByOrderIdAndStatus(String orderId, ConfigChangeRefundStatus status);

    /**
     * 根据订单ID查询所有退款记录
     */
    List<ConfigChangeRefundPo> findByOrderId(String orderId);

    /**
     * 更新退款状态
     */
    void updateStatus(String refundTaskNo, ConfigChangeRefundStatus status, String refundId, String failReason);

    /**
     * 更新人工审核状态
     */
    void updateManualAuditStatus(String refundTaskNo, String auditStatus);
}
