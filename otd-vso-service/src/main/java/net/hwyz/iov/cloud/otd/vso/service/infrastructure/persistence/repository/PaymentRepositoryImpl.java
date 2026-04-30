package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PaymentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 支付记录仓储实现
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPo save(PaymentPo paymentPo) {
        if (paymentPo.getId() == null) {
            paymentMapper.insert(paymentPo);
        } else {
            paymentMapper.updateById(paymentPo);
        }
        return paymentPo;
    }

    @Override
    public Optional<PaymentPo> findByPaymentNo(String paymentNo) {
        LambdaQueryWrapper<PaymentPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPo::getPaymentNo, paymentNo)
               .eq(PaymentPo::getRowValid, 1);
        return Optional.ofNullable(paymentMapper.selectOne(wrapper));
    }

    @Override
    public Optional<PaymentPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<PaymentPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PaymentPo::getOrderId, orderId)
               .eq(PaymentPo::getRowValid, 1)
               .orderByDesc(PaymentPo::getPayTime);
        return Optional.ofNullable(paymentMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String paymentId) {
        PaymentPo paymentPo = paymentMapper.selectById(paymentId);
        if (paymentPo != null) {
            paymentPo.setRowValid(0);
            paymentMapper.updateById(paymentPo);
        }
    }

}
