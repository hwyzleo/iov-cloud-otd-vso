package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.FinanceApplicationRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.FinanceApplicationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.FinanceApplicationPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 金融申请仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FinanceApplicationRepositoryImpl implements FinanceApplicationRepository {

    private final FinanceApplicationMapper financeApplicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinanceApplicationPo save(FinanceApplicationPo financeApplicationPo) {
        if (financeApplicationPo.getId() == null) {
            financeApplicationMapper.insert(financeApplicationPo);
        } else {
            financeApplicationMapper.updateById(financeApplicationPo);
        }
        return financeApplicationPo;
    }

    @Override
    public Optional<FinanceApplicationPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<FinanceApplicationPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinanceApplicationPo::getOrderId, orderId)
               .eq(FinanceApplicationPo::getRowValid, 1)
               .orderByDesc(FinanceApplicationPo::getApplyTime);
        return Optional.ofNullable(financeApplicationMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String financeApplicationId) {
        FinanceApplicationPo financeApplicationPo = financeApplicationMapper.selectById(financeApplicationId);
        if (financeApplicationPo != null) {
            financeApplicationPo.setRowValid(0);
            financeApplicationMapper.updateById(financeApplicationPo);
        }
    }

}
