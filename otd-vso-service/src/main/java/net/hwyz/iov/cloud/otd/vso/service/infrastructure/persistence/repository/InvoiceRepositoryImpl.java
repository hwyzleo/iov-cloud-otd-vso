package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.InvoiceRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.InvoiceMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.InvoicePo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 发票仓储实现
 */
@Repository
@RequiredArgsConstructor
public class InvoiceRepositoryImpl implements InvoiceRepository {

    private final InvoiceMapper invoiceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public InvoicePo save(InvoicePo invoicePo) {
        if (invoicePo.getId() == null) {
            invoiceMapper.insert(invoicePo);
        } else {
            invoiceMapper.updateById(invoicePo);
        }
        return invoicePo;
    }

    @Override
    public Optional<InvoicePo> findByOrderId(String orderId) {
        LambdaQueryWrapper<InvoicePo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoicePo::getOrderId, orderId)
               .eq(InvoicePo::getRowValid, 1);
        return Optional.ofNullable(invoiceMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String invoiceId) {
        InvoicePo invoicePo = invoiceMapper.selectById(invoiceId);
        if (invoicePo != null) {
            invoicePo.setRowValid(0);
            invoiceMapper.updateById(invoicePo);
        }
    }

}
