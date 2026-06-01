package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import java.util.List;
import java.util.Optional;

public interface SaleModelOptionPolicyRepository {
    Optional<SaleModelOptionPolicyPo> findById(Long id);
    Optional<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCode(String saleModelCode, String optionCode);
    List<SaleModelOptionPolicyPo> findBySaleModelCode(String saleModelCode);
    List<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCodes(String saleModelCode, List<String> optionCodes);
    void save(SaleModelOptionPolicyPo po);
    void update(SaleModelOptionPolicyPo po);
    void delete(Long id);
}
