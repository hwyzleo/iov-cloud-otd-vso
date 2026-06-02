package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
    public List<SaleModelOptionPolicyPo> findBySaleModelCodeAndModelCodeAndVariantCode(String saleModelCode, String modelCode, String variantCode) {
        LambdaQueryWrapper<SaleModelOptionPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelOptionPolicyPo::getSaleModelCode, saleModelCode)
               .eq(SaleModelOptionPolicyPo::getModelCode, modelCode)
               .eq(SaleModelOptionPolicyPo::getVariantCode, variantCode);
        return mapper.selectList(wrapper);
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

    @Override
    public int updateSaleStatusBySaleModelCode(String saleModelCode, String saleStatus, String modifyBy) {
        LambdaUpdateWrapper<SaleModelOptionPolicyPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SaleModelOptionPolicyPo::getSaleModelCode, saleModelCode)
               .set(SaleModelOptionPolicyPo::getSaleStatus, saleStatus)
               .set(SaleModelOptionPolicyPo::getModifyTime, new java.sql.Timestamp(System.currentTimeMillis()))
               .set(SaleModelOptionPolicyPo::getModifyBy, modifyBy);
        return mapper.update(null, wrapper);
    }

    @Override
    public int deleteBySaleModelCode(String saleModelCode) {
        return mapper.physicalDeleteBySaleModelCode(saleModelCode);
    }
}
