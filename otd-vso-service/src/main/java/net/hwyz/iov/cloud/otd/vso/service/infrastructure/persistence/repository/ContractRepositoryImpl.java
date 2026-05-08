package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ContractMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {

    private final ContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ContractPo save(ContractPo contractPo) {
        if (contractPo.getId() == null) {
            contractMapper.insertPo(contractPo);
        } else {
            contractMapper.updatePo(contractPo);
        }
        return contractPo;
    }

    @Override
    public Optional<ContractPo> findByOrderIdAndType(String orderId, String contractType) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("contractType", contractType);
        params.put("rowValid", 1);
        params.put("orderBy", "versionNo DESC");
        return contractMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String contractId) {
        ContractPo contractPo = contractMapper.selectPoById(Long.valueOf(contractId));
        if (contractPo != null) {
            contractPo.setRowValid(0);
            contractMapper.updatePo(contractPo);
        }
    }

}