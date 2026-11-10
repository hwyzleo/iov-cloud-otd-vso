package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.PaymentPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PaymentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 支付仓储实现（领域类型）
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Payment save(Payment payment) {
        PaymentPo po = PaymentPoConverter.fromDomain(payment);
        PaymentPo existing = paymentMapper.selectByPaymentNo(payment.getPaymentNo());
        if (existing == null) {
            paymentMapper.insertPo(po);
        } else {
            po.setId(existing.getId());
            paymentMapper.updatePo(po);
        }
        return payment;
    }

    @Override
    public Optional<Payment> findByPaymentNo(String paymentNo) {
        return Optional.ofNullable(paymentMapper.selectByPaymentNo(paymentNo))
                .map(PaymentPoConverter::toDomain);
    }

    @Override
    public List<Payment> findByOrderId(String orderId) {
        return paymentMapper.selectByOrderId(orderId).stream()
                .map(PaymentPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Payment> findActiveByOrderIdAndStage(String orderId, String paymentStage) {
        return Optional.ofNullable(paymentMapper.selectActiveByOrderIdAndStage(orderId, paymentStage))
                .map(PaymentPoConverter::toDomain);
    }

    @Override
    public List<Payment> findSuccessfulByOrderId(String orderId) {
        return paymentMapper.selectSuccessfulByOrderId(orderId).stream()
                .map(PaymentPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Payment> findSuccessfulByOrderIds(List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return List.of();
        }
        return paymentMapper.selectSuccessfulByOrderIds(orderIds).stream()
                .map(PaymentPoConverter::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int updateStatusConditional(String paymentNo, PaymentStatus targetStatus,
                                       List<PaymentStatus> expectedStatuses,
                                       String externalTradeNo, LocalDateTime payTime, String failReason) {
        return paymentMapper.updateStatusConditional(paymentNo, targetStatus.name(),
                expectedStatuses.stream().map(Enum::name).collect(Collectors.toList()),
                externalTradeNo, payTime, failReason);
    }

    @Override
    public void delete(String paymentId) {
        PaymentPo po = paymentMapper.selectByPaymentNo(paymentId);
        if (po != null && po.getId() != null) {
            paymentMapper.logicalDeletePo(po.getId());
        }
    }
}
