package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.SupplementaryPayment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.springframework.stereotype.Component;

/**
 * 补款 PO 转换器
 */
@Component
public class SupplementaryPaymentPoConverter {

    public SupplementaryPayment toDomain(SupplementaryPaymentPo po) {
        if (po == null) {
            return null;
        }
        return SupplementaryPayment.builder()
                .id(po.getId())
                .supplementaryNo(po.getSupplementaryNo())
                .orderId(po.getOrderId())
                .supplementaryAmount(Money.of(po.getSupplementaryAmount(), po.getCurrency()))
                .supplementaryStatus(po.getSupplementaryStatus() != null ? SupplementaryPaymentStatus.fromValue(po.getSupplementaryStatus()) : null)
                .configVersionNo(po.getConfigVersionNo())
                .paymentId(po.getPaymentId())
                .expireTime(po.getExpireTime())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    public SupplementaryPaymentPo toPo(SupplementaryPayment domain) {
        if (domain == null) {
            return null;
        }
        return SupplementaryPaymentPo.builder()
                .id(domain.getId())
                .supplementaryNo(domain.getSupplementaryNo())
                .orderId(domain.getOrderId())
                .supplementaryAmount(domain.getSupplementaryAmount().getAmount())
                .currency(domain.getSupplementaryAmount().getCurrency())
                .supplementaryStatus(domain.getSupplementaryStatus() != null ? domain.getSupplementaryStatus().getValue() : null)
                .configVersionNo(domain.getConfigVersionNo())
                .paymentId(domain.getPaymentId())
                .expireTime(domain.getExpireTime())
                .createTime(domain.getCreateTime())
                .updateTime(domain.getUpdateTime())
                .build();
    }
}