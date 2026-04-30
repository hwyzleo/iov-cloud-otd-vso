package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ContractPo;

import java.util.Optional;

/**
 * 合同仓储接口
 */
public interface ContractRepository {

    ContractPo save(ContractPo contractPo);

    Optional<ContractPo> findByOrderIdAndType(String orderId, String contractType);

    void delete(String contractId);

}
