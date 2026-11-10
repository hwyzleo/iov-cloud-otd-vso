package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundItemPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;

/**
 * 退款对象转换器（Domain ↔ PO）
 *
 * @author VSO Team
 */
public final class RefundPoConverter {

    private RefundPoConverter() {
    }

    public static Refund toDomain(RefundPo po) {
        if (po == null) {
            return null;
        }
        return Refund.builder()
                .refundId(po.getRefundId())
                .refundNo(po.getRefundNo())
                .orderId(po.getOrderId())
                .refundScene(po.getRefundScene())
                .refundAmount(po.getRefundAmount())
                .refundFee(po.getRefundFee())
                .refundStatus(RefundStatus.fromValue(po.getRefundStatus()))
                .externalRefundNo(po.getExternalRefundNo())
                .applyTime(po.getApplyTime())
                .refundTime(po.getRefundTime())
                .failReason(po.getFailReason())
                .build();
    }

    public static RefundPo fromDomain(Refund refund) {
        RefundPo po = new RefundPo();
        po.setRefundId(refund.getRefundId());
        po.setRefundNo(refund.getRefundNo());
        po.setOrderId(refund.getOrderId());
        po.setRefundScene(refund.getRefundScene());
        po.setRefundAmount(refund.getRefundAmount());
        po.setRefundFee(refund.getRefundFee());
        po.setRefundStatus(refund.getRefundStatus() == null ? null : refund.getRefundStatus().getValue());
        po.setExternalRefundNo(refund.getExternalRefundNo());
        po.setApplyTime(refund.getApplyTime());
        po.setRefundTime(refund.getRefundTime());
        po.setFailReason(refund.getFailReason());
        return po;
    }

    public static RefundItem toDomain(RefundItemPo po) {
        if (po == null) {
            return null;
        }
        return RefundItem.builder()
                .refundItemId(po.getRefundItemId())
                .refundId(po.getRefundId())
                .paymentId(po.getPaymentId())
                .paymentNo(po.getPaymentNo())
                .refundAmount(po.getRefundAmount())
                .refundStatus(RefundStatus.fromValue(po.getRefundStatus()))
                .externalRefundNo(po.getExternalRefundNo())
                .failReason(po.getFailReason())
                .refundTime(po.getRefundTime())
                .build();
    }

    public static RefundItemPo fromDomain(RefundItem item) {
        RefundItemPo po = new RefundItemPo();
        po.setRefundItemId(item.getRefundItemId());
        po.setRefundId(item.getRefundId());
        po.setPaymentId(item.getPaymentId());
        po.setPaymentNo(item.getPaymentNo());
        po.setRefundAmount(item.getRefundAmount());
        po.setRefundStatus(item.getRefundStatus() == null ? null : item.getRefundStatus().getValue());
        po.setExternalRefundNo(item.getExternalRefundNo());
        po.setFailReason(item.getFailReason());
        po.setRefundTime(item.getRefundTime());
        return po;
    }
}
