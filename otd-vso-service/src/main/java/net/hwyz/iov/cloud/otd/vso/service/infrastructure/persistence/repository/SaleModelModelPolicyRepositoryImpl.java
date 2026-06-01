package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelModelPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelModelPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelModelPolicyPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Model 销售策略仓储实现
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Repository
@RequiredArgsConstructor
public class SaleModelModelPolicyRepositoryImpl implements SaleModelModelPolicyRepository {

    private final SaleModelModelPolicyMapper mapper;

    @Override
    public Optional<SaleModelModelPolicyPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<SaleModelModelPolicyPo> findBySaleModelCodeAndModelCode(String saleModelCode, String modelCode) {
        LambdaQueryWrapper<SaleModelModelPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelModelPolicyPo::getSaleModelCode, saleModelCode)
               .eq(SaleModelModelPolicyPo::getModelCode, modelCode);
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public List<SaleModelModelPolicyPo> findBySaleModelCode(String saleModelCode) {
        LambdaQueryWrapper<SaleModelModelPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelModelPolicyPo::getSaleModelCode, saleModelCode);
        return mapper.selectList(wrapper);
    }

    @Override
    public int insert(SaleModelModelPolicyPo entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(SaleModelModelPolicyPo entity) {
        LambdaUpdateWrapper<SaleModelModelPolicyPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SaleModelModelPolicyPo::getId, entity.getId())
               .set(SaleModelModelPolicyPo::getSaleStatus, entity.getSaleStatus())
               .set(SaleModelModelPolicyPo::getAvailableRegions, entity.getAvailableRegions())
               .set(SaleModelModelPolicyPo::getChannels, entity.getChannels())
               .set(SaleModelModelPolicyPo::getMarketingName, entity.getMarketingName())
               .set(SaleModelModelPolicyPo::getMarketingImage, entity.getMarketingImage())
               .set(SaleModelModelPolicyPo::getMarketingCopy, entity.getMarketingCopy())
               .set(SaleModelModelPolicyPo::getSortWeight, entity.getSortWeight())
               .set(SaleModelModelPolicyPo::getEffectiveFrom, entity.getEffectiveFrom())
               .set(SaleModelModelPolicyPo::getEffectiveTo, entity.getEffectiveTo())
               .set(SaleModelModelPolicyPo::getDescription, entity.getDescription())
               .set(SaleModelModelPolicyPo::getModifyTime, entity.getModifyTime())
               .set(SaleModelModelPolicyPo::getModifyBy, entity.getModifyBy());
        return mapper.update(null, wrapper);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }

    @Override
    public int updateSaleStatusBySaleModelCode(String saleModelCode, String saleStatus, String modifyBy) {
        LambdaUpdateWrapper<SaleModelModelPolicyPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SaleModelModelPolicyPo::getSaleModelCode, saleModelCode)
               .set(SaleModelModelPolicyPo::getSaleStatus, saleStatus)
               .set(SaleModelModelPolicyPo::getModifyTime, new java.sql.Timestamp(System.currentTimeMillis()))
               .set(SaleModelModelPolicyPo::getModifyBy, modifyBy);
        return mapper.update(null, wrapper);
    }

    @Override
    public int deleteBySaleModelCode(String saleModelCode) {
        return mapper.physicalDeleteBySaleModelCode(saleModelCode);
    }
}
