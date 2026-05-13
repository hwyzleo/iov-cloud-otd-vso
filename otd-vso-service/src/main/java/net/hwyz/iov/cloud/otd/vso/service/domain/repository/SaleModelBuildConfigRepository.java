package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型生产配置关联仓储接口。
 */
public interface SaleModelBuildConfigRepository {

    Optional<SaleModelBuildConfigPo> findById(Long id);

    List<SaleModelBuildConfigPo> findBySaleModelCode(String saleModelCode);

    Optional<SaleModelBuildConfigPo> findBySaleModelCodeAndBuildConfigCode(String saleModelCode, String buildConfigCode);

    Optional<SaleModelBuildConfigPo> findBySaleModelCodeAndBuildConfigCodeIncludeDeleted(String saleModelCode, String buildConfigCode);

    int insert(SaleModelBuildConfigPo entity);

    int update(SaleModelBuildConfigPo entity);

    int physicalDeleteByIds(Long[] ids);

    int physicalDeleteBySaleModelCodeAndBuildConfigCode(String saleModelCode, String buildConfigCode);

    int physicalDeleteBySaleModelCode(String saleModelCode);

    int countBySaleModelCode(String saleModelCode);
}