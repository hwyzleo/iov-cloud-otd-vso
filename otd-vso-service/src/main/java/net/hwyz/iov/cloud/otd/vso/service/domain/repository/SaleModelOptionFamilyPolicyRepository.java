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
}
