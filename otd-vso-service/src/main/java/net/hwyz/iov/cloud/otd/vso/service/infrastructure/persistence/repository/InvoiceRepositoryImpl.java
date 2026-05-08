package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.InvoiceRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.InvoiceMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.InvoicePo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoicePo save(InvoicePo invoicePo) {
        if (invoicePo.getId() == null) {
            invoiceMapper.insertPo(invoicePo);
        } else {
            invoiceMapper.updatePo(invoicePo);
        }
        return invoicePo;
    }

    @Override
    public Optional<InvoicePo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        return invoiceMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String invoiceId) {
        InvoicePo invoicePo = invoiceMapper.selectPoById(Long.valueOf(invoiceId));
        if (invoicePo != null) {
            invoicePo.setRowValid(0);
            invoiceMapper.updatePo(invoicePo);
        }
    }

}