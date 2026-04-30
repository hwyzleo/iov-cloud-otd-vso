package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SubsidyApplicationRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SubsidyApplicationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SubsidyApplicationPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 补贴申请仓储实现
 */
@Repository
@RequiredArgsConstructor
public class SubsidyApplicationRepositoryImpl implements SubsidyApplicationRepository {

    private final SubsidyApplicationMapper subsidyApplicationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SubsidyApplicationPo save(SubsidyApplicationPo subsidyApplicationPo) {
        if (subsidyApplicationPo.getId() == null) {
            subsidyApplicationMapper.insert(subsidyApplicationPo);
        } else {
            subsidyApplicationMapper.updateById(subsidyApplicationPo);
        }
        return subsidyApplicationPo;
    }

    @Override
    public Optional<SubsidyApplicationPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<SubsidyApplicationPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubsidyApplicationPo::getOrderId, orderId)
               .eq(SubsidyApplicationPo::getRowValid, 1)
               .orderByDesc(SubsidyApplicationPo::getApplyTime);
        return Optional.ofNullable(subsidyApplicationMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String subsidyApplicationId) {
        SubsidyApplicationPo subsidyApplicationPo = subsidyApplicationMapper.selectById(subsidyApplicationId);
        if (subsidyApplicationPo != null) {
            subsidyApplicationPo.setRowValid(0);
            subsidyApplicationMapper.updateById(subsidyApplicationPo);
        }
    }

}
