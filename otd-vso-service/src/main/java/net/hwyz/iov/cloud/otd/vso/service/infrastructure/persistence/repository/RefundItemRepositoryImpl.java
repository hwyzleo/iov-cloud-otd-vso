package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.RefundPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.RefundItemMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundItemPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款明细仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class RefundItemRepositoryImpl implements RefundItemRepository {

    private final RefundItemMapper refundItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundItem save(RefundItem item) {
        RefundItemPo po = RefundPoConverter.fromDomain(item);
        RefundItemPo existing = refundItemMapper.selectByRefundItemId(item.getRefundItemId());
        if (existing == null) {
            refundItemMapper.insertPo(po);
        } else {
            po.setId(existing.getId());
            refundItemMapper.updatePo(po);
        }
        return item;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RefundItem> saveAll(List<RefundItem> items) {
        items.forEach(this::save);
        return items;
    }

    @Override
    public List<RefundItem> findByRefundId(String refundId) {
        return refundItemMapper.selectByRefundId(refundId).stream()
                .map(RefundPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundItem> findByRefundIdAndStatus(String refundId, RefundStatus refundStatus) {
        return refundItemMapper.selectByRefundIdAndStatus(refundId, refundStatus.getValue()).stream()
                .map(RefundPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundItem> findByPaymentIdAndStatus(String paymentId, RefundStatus refundStatus) {
        return refundItemMapper.selectByPaymentIdAndStatus(paymentId, refundStatus.getValue()).stream()
                .map(RefundPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<RefundItem> findByRefundItemId(String refundItemId) {
        return Optional.ofNullable(refundItemMapper.selectByRefundItemId(refundItemId))
                .map(RefundPoConverter::toDomain);
    }
}
