package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.RefundPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.RefundItemMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.RefundMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 退款主单仓储实现（领域类型）
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundMapper refundMapper;
    private final RefundItemMapper refundItemMapper;
    private final RefundItemRepository refundItemRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Refund save(Refund refund) {
        RefundPo po = RefundPoConverter.fromDomain(refund);
        RefundPo existing = refundMapper.selectByRefundNo(refund.getRefundNo());
        if (existing == null) {
            refundMapper.insertPo(po);
        } else {
            po.setId(existing.getId());
            refundMapper.updatePo(po);
        }
        if (refund.getItems() != null && !refund.getItems().isEmpty()) {
            refundItemRepository.saveAll(refund.getItems());
        }
        return refund;
    }

    @Override
    public Optional<Refund> findByRefundNo(String refundNo) {
        RefundPo po = refundMapper.selectByRefundNo(refundNo);
        return Optional.ofNullable(po).map(this::assemble);
    }

    @Override
    public Optional<Refund> findByRefundId(String refundId) {
        RefundPo po = refundMapper.selectPoByMap(java.util.Map.of("refundId", refundId)).stream()
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(po).map(this::assemble);
    }

    @Override
    public Optional<Refund> findActiveByOrderId(String orderId) {
        RefundPo po = refundMapper.selectActiveByOrderId(orderId);
        return Optional.ofNullable(po).map(this::assemble);
    }

    @Override
    public List<Refund> findByOrderId(String orderId) {
        return refundMapper.selectByOrderId(orderId).stream()
                .map(this::assemble)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Refund refund) {
        RefundPo existing = refundMapper.selectByRefundNo(refund.getRefundNo());
        if (existing == null) {
            return;
        }
        RefundPo po = RefundPoConverter.fromDomain(refund);
        po.setId(existing.getId());
        refundMapper.updatePo(po);
    }

    private Refund assemble(RefundPo po) {
        Refund refund = RefundPoConverter.toDomain(po);
        if (refund != null) {
            List<RefundItem> items = refundItemMapper.selectByRefundId(po.getRefundId()).stream()
                    .map(RefundPoConverter::toDomain)
                    .collect(Collectors.toList());
            refund.setItems(items);
        }
        return refund;
    }
}
