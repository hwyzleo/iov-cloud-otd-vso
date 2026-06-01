package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelModelPolicyPo;

import java.util.List;
import java.util.Optional;

/**
 * Model 销售策略仓储接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
public interface SaleModelModelPolicyRepository {

    Optional<SaleModelModelPolicyPo> findById(Long id);

    Optional<SaleModelModelPolicyPo> findBySaleModelCodeAndModelCode(String saleModelCode, String modelCode);

    List<SaleModelModelPolicyPo> findBySaleModelCode(String saleModelCode);

    int insert(SaleModelModelPolicyPo entity);

    int update(SaleModelModelPolicyPo entity);

    int deleteById(Long id);
}
