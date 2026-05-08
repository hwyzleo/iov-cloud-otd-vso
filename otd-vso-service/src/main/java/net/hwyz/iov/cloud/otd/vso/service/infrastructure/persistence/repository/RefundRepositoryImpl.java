package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.RefundMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundMapper refundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundPo save(RefundPo refundPo) {
        if (refundPo.getId() == null) {
            refundMapper.insertPo(refundPo);
        } else {
            refundMapper.updatePo(refundPo);
        }
        return refundPo;
    }

    @Override
    public Optional<RefundPo> findByRefundNo(String refundNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("refundNo", refundNo);
        params.put("rowValid", 1);
        return refundMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String refundId) {
        RefundPo refundPo = refundMapper.selectPoById(Long.valueOf(refundId));
        if (refundPo != null) {
            refundPo.setRowValid(0);
            refundMapper.updatePo(refundPo);
        }
    }

}