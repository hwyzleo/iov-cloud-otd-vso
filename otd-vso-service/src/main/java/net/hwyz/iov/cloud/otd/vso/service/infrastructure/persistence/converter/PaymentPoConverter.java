package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;

/**
 * 支付对象转换器（Domain ↔ PO）
 *
 * @author VSO Team
 */
public final class PaymentPoConverter {

    private PaymentPoConverter() {
    }

    public static Payment toDomain(PaymentPo po) {
        if (po == null) {
            return null;
        }
        return Payment.builder()
                .paymentId(po.getPaymentId())
                .paymentNo(po.getPaymentNo())
                .orderId(po.getOrderId())
                .paymentStage(po.getPaymentStage())
                .paymentChannel(po.getPaymentChannel())
                .paymentAmount(po.getPaymentAmount())
                .paymentStatus(po.getPaymentStatus() == null ? null : PaymentStatus.valueOf(po.getPaymentStatus()))
                .initiatorRole(po.getInitiatorRole())
                .initiatorId(po.getInitiatorId())
                .externalTradeNo(po.getExternalTradeNo())
                .payTime(po.getPayTime())
                .expireTime(po.getExpireTime())
                .failReason(po.getFailReason())
                .build();
    }

    public static PaymentPo fromDomain(Payment payment) {
        PaymentPo po = new PaymentPo();
        po.setPaymentId(payment.getPaymentId());
        po.setPaymentNo(payment.getPaymentNo());
        po.setOrderId(payment.getOrderId());
        po.setPaymentStage(payment.getPaymentStage());
        po.setPaymentChannel(payment.getPaymentChannel());
        po.setPaymentAmount(payment.getPaymentAmount());
        po.setPaymentStatus(payment.getPaymentStatus() == null ? null : payment.getPaymentStatus().name());
        po.setInitiatorRole(payment.getInitiatorRole());
        po.setInitiatorId(payment.getInitiatorId());
        po.setAuthorizedFlag(0);
        po.setExternalTradeNo(payment.getExternalTradeNo());
        po.setPayTime(payment.getPayTime());
        po.setExpireTime(payment.getExpireTime());
        po.setFailReason(payment.getFailReason());
        return po;
    }
}
