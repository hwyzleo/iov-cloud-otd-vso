package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ApprovalRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ApprovalMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 审批单仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ApprovalRepositoryImpl implements ApprovalRepository {

    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalPo save(ApprovalPo approvalPo) {
        if (approvalPo.getId() == null) {
            approvalMapper.insert(approvalPo);
        } else {
            approvalMapper.updateById(approvalPo);
        }
        return approvalPo;
    }

    @Override
    public Optional<ApprovalPo> findByApprovalNo(String approvalNo) {
        LambdaQueryWrapper<ApprovalPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalPo::getApprovalNo, approvalNo)
               .eq(ApprovalPo::getRowValid, 1);
        return Optional.ofNullable(approvalMapper.selectOne(wrapper));
    }

    @Override
    public Optional<ApprovalPo> findByOrderId(String orderId) {
        LambdaQueryWrapper<ApprovalPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalPo::getOrderId, orderId)
               .eq(ApprovalPo::getRowValid, 1)
               .orderByDesc(ApprovalPo::getSubmitTime);
        return Optional.ofNullable(approvalMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String approvalId) {
        ApprovalPo approvalPo = approvalMapper.selectById(approvalId);
        if (approvalPo != null) {
            approvalPo.setRowValid(0);
            approvalMapper.updateById(approvalPo);
        }
    }

}
