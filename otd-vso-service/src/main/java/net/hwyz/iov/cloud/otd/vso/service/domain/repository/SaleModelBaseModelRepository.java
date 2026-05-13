package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBaseModelPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型基础车型关联仓储接口。
 */
public interface SaleModelBaseModelRepository {

    Optional<SaleModelBaseModelPo> findById(Long id);

    List<SaleModelBaseModelPo> findBySaleModelCode(String saleModelCode);

    Optional<SaleModelBaseModelPo> findBySaleModelCodeAndBaseModelCode(String saleModelCode, String baseModelCode);

    Optional<SaleModelBaseModelPo> findBySaleModelCodeAndBaseModelCodeIncludeDeleted(String saleModelCode, String baseModelCode);

    int insert(SaleModelBaseModelPo entity);

    int update(SaleModelBaseModelPo entity);

    int physicalDeleteByIds(Long[] ids);

    int physicalDeleteBySaleModelCodeAndBaseModelCode(String saleModelCode, String baseModelCode);
}