package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.FinanceApplicationPo;

import java.util.Optional;

/**
 * 金融申请仓储接口
 */
public interface FinanceApplicationRepository {

    FinanceApplicationPo save(FinanceApplicationPo financeApplicationPo);

    Optional<FinanceApplicationPo> findByOrderId(String orderId);

    void delete(String financeApplicationId);

}
