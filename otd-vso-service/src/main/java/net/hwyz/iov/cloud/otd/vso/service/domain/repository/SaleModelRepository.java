package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型仓储接口。
 */
public interface SaleModelRepository {

    Optional<SaleModelPo> findById(Long id);

    Optional<SaleModelPo> findBySaleModelCode(String saleModelCode);

    List<SaleModelPo> findAll();

    List<SaleModelPo> findByCondition(SaleModelQuery query);

    boolean existsBySaleModelCodeExcludeId(String saleModelCode, Long excludeId);

    int insert(SaleModelPo entity);

    int update(SaleModelPo entity);

    int physicalDeleteByIds(Long[] ids);
}
