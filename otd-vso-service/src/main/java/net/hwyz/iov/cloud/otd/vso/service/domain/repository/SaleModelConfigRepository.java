package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;

import java.util.List;
import java.util.Optional;

/**
 * 销售车型配置仓储接口。
 */
public interface SaleModelConfigRepository {

    Optional<SaleModelConfigPo> findById(Long id);

    List<SaleModelConfigPo> findBySaleCode(String saleCode);

    List<SaleModelConfigPo> findBySaleModelId(Long saleModelId);

    Optional<SaleModelConfigPo> findByIdAndSaleCode(Long id, String saleCode);

    int insert(SaleModelConfigPo entity);

    int update(SaleModelConfigPo entity);

    int physicalDeleteBySaleCodeAndIds(String saleCode, Long[] ids);
}
