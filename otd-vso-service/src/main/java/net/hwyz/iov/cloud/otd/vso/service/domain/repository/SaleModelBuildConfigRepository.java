package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型生产配置关联仓储接口。
 */
public interface SaleModelBuildConfigRepository {

    Optional<SaleModelBuildConfigPo> findById(Long id);

    List<SaleModelBuildConfigPo> findBySaleCode(String saleCode);

    Optional<SaleModelBuildConfigPo> findBySaleCodeAndBuildConfigCode(String saleCode, String buildConfigCode);

    Optional<SaleModelBuildConfigPo> findBySaleCodeAndBuildConfigCodeIncludeDeleted(String saleCode, String buildConfigCode);

    int insert(SaleModelBuildConfigPo entity);

    int update(SaleModelBuildConfigPo entity);

    int physicalDeleteByIds(Long[] ids);

    int physicalDeleteBySaleCodeAndBuildConfigCode(String saleCode, String buildConfigCode);

    int physicalDeleteBySaleCode(String saleCode);

    int countBySaleCode(String saleCode);
}