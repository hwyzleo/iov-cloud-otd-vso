package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.RefundMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.RefundPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 退款记录仓储实现
 */
@Repository
@RequiredArgsConstructor
public class RefundRepositoryImpl implements RefundRepository {

    private final RefundMapper refundMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundPo save(RefundPo refundPo) {
        if (refundPo.getId() == null) {
            refundMapper.insert(refundPo);
        } else {
            refundMapper.updateById(refundPo);
        }
        return refundPo;
    }

    @Override
    public Optional<RefundPo> findByRefundNo(String refundNo) {
        LambdaQueryWrapper<RefundPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefundPo::getRefundNo, refundNo)
               .eq(RefundPo::getRowValid, 1);
        return Optional.ofNullable(refundMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String refundId) {
        RefundPo refundPo = refundMapper.selectById(refundId);
        if (refundPo != null) {
            refundPo.setRowValid(0);
            refundMapper.updateById(refundPo);
        }
    }

}
