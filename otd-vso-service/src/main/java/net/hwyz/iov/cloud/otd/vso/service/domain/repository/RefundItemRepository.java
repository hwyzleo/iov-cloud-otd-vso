package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;

import java.util.List;
import java.util.Optional;

/**
 * 退款原支付明细仓储接口（领域类型）
 *
 * @author VSO Team
 */
public interface RefundItemRepository {

    RefundItem save(RefundItem item);

    List<RefundItem> saveAll(List<RefundItem> items);

    List<RefundItem> findByRefundId(String refundId);

    List<RefundItem> findByRefundIdAndStatus(String refundId, RefundStatus refundStatus);

    List<RefundItem> findByPaymentIdAndStatus(String paymentId, RefundStatus refundStatus);

    Optional<RefundItem> findByRefundItemId(String refundItemId);
}
