package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionFamilyPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelOptionFamilyPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionFamilyPolicyPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelOptionFamilyPolicyRepositoryImpl implements SaleModelOptionFamilyPolicyRepository {
    private final SaleModelOptionFamilyPolicyMapper mapper;

    @Override
    public Optional<SaleModelOptionFamilyPolicyPo> findBySaleModelCodeAndFamilyCode(String saleModelCode, String optionFamilyCode) {
        return Optional.ofNullable(mapper.selectBySaleModelCodeAndFamilyCode(saleModelCode, optionFamilyCode));
    }

    @Override
    public List<SaleModelOptionFamilyPolicyPo> findBySaleModelCode(String saleModelCode) {
        return mapper.selectBySaleModelCode(saleModelCode);
    }

    @Override
    public void save(SaleModelOptionFamilyPolicyPo po) {
        mapper.insert(po);
    }

    @Override
    public void update(SaleModelOptionFamilyPolicyPo po) {
        mapper.updateById(po);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
