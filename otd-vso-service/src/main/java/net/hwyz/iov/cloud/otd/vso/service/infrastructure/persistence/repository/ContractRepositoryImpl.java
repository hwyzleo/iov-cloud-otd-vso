package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ContractMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 合同仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {

    private final ContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractPo save(ContractPo contractPo) {
        if (contractPo.getId() == null) {
            contractMapper.insert(contractPo);
        } else {
            contractMapper.updateById(contractPo);
        }
        return contractPo;
    }

    @Override
    public Optional<ContractPo> findByOrderIdAndType(String orderId, String contractType) {
        LambdaQueryWrapper<ContractPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractPo::getOrderId, orderId)
               .eq(ContractPo::getContractType, contractType)
               .eq(ContractPo::getRowValid, 1)
               .orderByDesc(ContractPo::getVersionNo);
        return Optional.ofNullable(contractMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String contractId) {
        ContractPo contractPo = contractMapper.selectById(contractId);
        if (contractPo != null) {
            contractPo.setRowValid(0);
            contractMapper.updateById(contractPo);
        }
    }

}
