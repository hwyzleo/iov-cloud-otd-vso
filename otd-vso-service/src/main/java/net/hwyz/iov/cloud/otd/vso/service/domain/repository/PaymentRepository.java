package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;

import java.util.Optional;

/**
 * 支付记录仓储接口
 */
public interface PaymentRepository {

    PaymentPo save(PaymentPo paymentPo);

    Optional<PaymentPo> findByPaymentNo(String paymentNo);

    Optional<PaymentPo> findByOrderId(String orderId);

    void delete(String paymentId);

}
