package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionFamilyPolicyPo;
import java.util.List;
import java.util.Optional;

public interface SaleModelOptionFamilyPolicyRepository {
    Optional<SaleModelOptionFamilyPolicyPo> findBySaleModelCodeAndFamilyCode(String saleModelCode, String optionFamilyCode);
    List<SaleModelOptionFamilyPolicyPo> findBySaleModelCode(String saleModelCode);
    void save(SaleModelOptionFamilyPolicyPo po);
    void update(SaleModelOptionFamilyPolicyPo po);
    void delete(Long id);

    /**
     * 根据 saleModelCode 物理删除所有关联策略
     * @param saleModelCode 销售车型编码
     * @return 删除数量
     */
    int deleteBySaleModelCode(String saleModelCode);
}
