package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import java.util.List;
import java.util.Optional;

public interface SaleModelConfigPolicyRepository {
    Optional<SaleModelConfigPolicyPo> findBySaleModelCodeAndConfigCode(String saleModelCode, String configurationCode);
    List<SaleModelConfigPolicyPo> findBySaleModelCode(String saleModelCode);
    boolean existsBySaleModelCode(String saleModelCode);
    void save(SaleModelConfigPolicyPo po);
    void update(SaleModelConfigPolicyPo po);
    void reactivate(Long id, String status);
    void delete(Long id);
}
