package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PaymentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentMapper paymentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPo save(PaymentPo paymentPo) {
        if (paymentPo.getId() == null) {
            paymentMapper.insertPo(paymentPo);
        } else {
            paymentMapper.updatePo(paymentPo);
        }
        return paymentPo;
    }

    @Override
    public Optional<PaymentPo> findByPaymentNo(String paymentNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentNo", paymentNo);
        params.put("rowValid", 1);
        return paymentMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public Optional<PaymentPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "payTime DESC");
        return paymentMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String paymentId) {
        PaymentPo paymentPo = paymentMapper.selectPoById(Long.valueOf(paymentId));
        if (paymentPo != null) {
            paymentPo.setRowValid(0);
            paymentMapper.updatePo(paymentPo);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String paymentNo, String status, String externalTradeNo, LocalDateTime payTime) {
        Optional<PaymentPo> paymentOpt = findByPaymentNo(paymentNo);
        if (paymentOpt.isPresent()) {
            PaymentPo paymentPo = paymentOpt.get();
            paymentPo.setPaymentStatus(status);
            paymentPo.setExternalTradeNo(externalTradeNo);
            paymentPo.setPayTime(payTime);
            paymentMapper.updatePo(paymentPo);
        }
    }

}