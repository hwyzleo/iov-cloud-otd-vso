package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.api.enums.ConfigChangeRefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.ConfigChangeRefund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigChangeRefundPo;
import org.springframework.stereotype.Component;

/**
 * 改配退款 PO 转换器
 */
@Component
public class ConfigChangeRefundPoConverter {

    public ConfigChangeRefund toDomain(ConfigChangeRefundPo po) {
        if (po == null) {
            return null;
        }
        return ConfigChangeRefund.builder()
                .id(po.getId())
                .refundTaskNo(po.getRefundTaskNo())
                .orderId(po.getOrderId())
                .refundAmount(Money.of(po.getRefundAmount(), po.getCurrency()))
                .refundStatus(po.getRefundStatus() != null ? ConfigChangeRefundStatus.fromValue(po.getRefundStatus()) : null)
                .configVersionNo(po.getConfigVersionNo())
                .refundId(po.getRefundId())
                .failReason(po.getFailReason())
                .manualAuditStatus(po.getManualAuditStatus())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }

    public ConfigChangeRefundPo toPo(ConfigChangeRefund domain) {
        if (domain == null) {
            return null;
        }
        return ConfigChangeRefundPo.builder()
                .id(domain.getId())
                .refundTaskNo(domain.getRefundTaskNo())
                .orderId(domain.getOrderId())
                .refundAmount(domain.getRefundAmount().getAmount())
                .currency(domain.getRefundAmount().getCurrency())
                .refundStatus(domain.getRefundStatus() != null ? domain.getRefundStatus().getValue() : null)
                .configVersionNo(domain.getConfigVersionNo())
                .refundId(domain.getRefundId())
                .failReason(domain.getFailReason())
                .manualAuditStatus(domain.getManualAuditStatus())
                .createTime(domain.getCreateTime())
                .updateTime(domain.getUpdateTime())
                .build();
    }
}