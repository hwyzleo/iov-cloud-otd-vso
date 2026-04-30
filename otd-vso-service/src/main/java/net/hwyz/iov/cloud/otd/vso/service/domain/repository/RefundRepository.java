package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;

import java.util.Optional;

/**
 * 退款记录仓储接口
 */
public interface RefundRepository {

    RefundPo save(RefundPo refundPo);

    Optional<RefundPo> findByRefundNo(String refundNo);

    void delete(String refundId);

}
