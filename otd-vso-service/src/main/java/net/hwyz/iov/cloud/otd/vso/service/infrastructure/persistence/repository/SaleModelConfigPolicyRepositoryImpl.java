package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelConfigPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPolicyPo;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelConfigPolicyRepositoryImpl implements SaleModelConfigPolicyRepository {
    private final SaleModelConfigPolicyMapper mapper;

    @Override
    public Optional<SaleModelConfigPolicyPo> findBySaleModelCodeAndConfigCode(String saleModelCode, String configurationCode) {
        return Optional.ofNullable(mapper.selectBySaleModelCodeAndConfigCode(saleModelCode, configurationCode));
    }

    @Override
    public List<SaleModelConfigPolicyPo> findBySaleModelCode(String saleModelCode) {
        return mapper.selectBySaleModelCode(saleModelCode);
    }

    @Override
    public boolean existsBySaleModelCode(String saleModelCode) {
        LambdaQueryWrapper<SaleModelConfigPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelConfigPolicyPo::getSaleModelCode, saleModelCode);
        return mapper.selectCount(wrapper) > 0;
    }

    @Override
    public void save(SaleModelConfigPolicyPo po) {
        mapper.insert(po);
    }

    @Override
    public void update(SaleModelConfigPolicyPo po) {
        mapper.updateById(po);
    }

    @Override
    public void reactivate(Long id, String status) {
        mapper.reactivateById(id, status, new Date());
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
