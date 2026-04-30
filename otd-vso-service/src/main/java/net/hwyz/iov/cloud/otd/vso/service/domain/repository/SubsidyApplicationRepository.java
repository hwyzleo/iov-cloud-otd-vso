package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SubsidyApplicationPo;

import java.util.Optional;

/**
 * 补贴申请仓储接口
 */
public interface SubsidyApplicationRepository {

    SubsidyApplicationPo save(SubsidyApplicationPo subsidyApplicationPo);

    Optional<SubsidyApplicationPo> findByOrderId(String orderId);

    void delete(String subsidyApplicationId);

}
