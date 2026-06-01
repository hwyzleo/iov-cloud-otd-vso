package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelOptionPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelOptionPolicyRepositoryImpl implements SaleModelOptionPolicyRepository {
    private final SaleModelOptionPolicyMapper mapper;

    @Override
    public Optional<SaleModelOptionPolicyPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCode(String saleModelCode, String optionCode) {
        return Optional.ofNullable(mapper.selectBySaleModelCodeAndOptionCode(saleModelCode, optionCode));
    }

    @Override
    public List<SaleModelOptionPolicyPo> findBySaleModelCode(String saleModelCode) {
        return mapper.selectBySaleModelCode(saleModelCode);
    }

    @Override
    public List<SaleModelOptionPolicyPo> findBySaleModelCodeAndOptionCodes(String saleModelCode, List<String> optionCodes) {
        return mapper.selectBySaleModelCodeAndOptionCodes(saleModelCode, optionCodes);
    }

    @Override
    public void save(SaleModelOptionPolicyPo po) {
        mapper.insert(po);
    }

    @Override
    public void update(SaleModelOptionPolicyPo po) {
        mapper.updateByIdDirect(po);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
