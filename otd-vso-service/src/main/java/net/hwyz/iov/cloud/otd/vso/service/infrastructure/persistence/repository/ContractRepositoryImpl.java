package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.ContractPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ContractMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 合同仓储实现（领域类型，OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class ContractRepositoryImpl implements ContractRepository {

    private final ContractMapper contractMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Contract save(Contract contract) {
        ContractPo po = ContractPoConverter.fromDomain(contract, null);
        ContractPo existing = contractMapper.selectByContractId(contract.getContractId());
        if (existing == null) {
            contractMapper.insertPo(po);
        } else {
            po.setId(existing.getId());
            contractMapper.updatePo(po);
        }
        return contract;
    }

    @Override
    public Optional<Contract> findByOrderIdAndType(String orderId, String contractType) {
        return Optional.ofNullable(contractMapper.selectByOrderIdAndType(orderId, contractType))
                .map(ContractPoConverter::toDomain);
    }

    @Override
    public Optional<Contract> findBySignNo(String signNo) {
        if (signNo == null || signNo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(contractMapper.selectBySignNo(signNo))
                .map(ContractPoConverter::toDomain);
    }

    @Override
    public Optional<Contract> findByContractNo(String contractNo) {
        if (contractNo == null || contractNo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(contractMapper.selectByContractNo(contractNo))
                .map(ContractPoConverter::toDomain);
    }

}
