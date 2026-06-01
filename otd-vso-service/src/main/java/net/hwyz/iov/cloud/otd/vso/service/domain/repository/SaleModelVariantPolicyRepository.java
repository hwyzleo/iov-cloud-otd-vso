package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;

import java.util.List;
import java.util.Optional;

/**
 * Variant 销售策略仓储接口
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
public interface SaleModelVariantPolicyRepository {

    Optional<SaleModelVariantPolicyPo> findById(Long id);

    Optional<SaleModelVariantPolicyPo> findBySaleModelCodeAndVariantCode(String saleModelCode, String variantCode);

    List<SaleModelVariantPolicyPo> findBySaleModelCode(String saleModelCode);

    List<SaleModelVariantPolicyPo> findBySaleModelCodeAndModelCode(String saleModelCode, String modelCode);

    int insert(SaleModelVariantPolicyPo entity);

    int update(SaleModelVariantPolicyPo entity);

    int deleteById(Long id);
}
