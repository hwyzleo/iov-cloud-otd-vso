package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.InvoicePo;

import java.util.Optional;

/**
 * 发票仓储接口
 */
public interface InvoiceRepository {

    InvoicePo save(InvoicePo invoicePo);

    Optional<InvoicePo> findByOrderId(String orderId);

    void delete(String invoiceId);

}
