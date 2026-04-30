package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型仓储接口。
 */
public interface SaleModelRepository {

    Optional<SaleModelPo> findById(Long id);

    Optional<SaleModelPo> findBySaleCode(String saleCode);

    List<SaleModelPo> findAll();

    List<SaleModelPo> findByCondition(String saleCode, String modelName, java.time.Instant beginTime, java.time.Instant endTime);

    boolean existsBySaleCodeExcludeId(String saleCode, Long excludeId);

    int insert(SaleModelPo entity);

    int update(SaleModelPo entity);

    int physicalDeleteByIds(Long[] ids);
}
