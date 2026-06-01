package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelVariantPolicyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Variant 销售策略仓储实现
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Repository
@RequiredArgsConstructor
public class SaleModelVariantPolicyRepositoryImpl implements SaleModelVariantPolicyRepository {

    private final SaleModelVariantPolicyMapper mapper;

    @Override
    public Optional<SaleModelVariantPolicyPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public Optional<SaleModelVariantPolicyPo> findBySaleModelCodeAndVariantCode(String saleModelCode, String variantCode) {
        LambdaQueryWrapper<SaleModelVariantPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelVariantPolicyPo::getSaleModelCode, saleModelCode)
               .eq(SaleModelVariantPolicyPo::getVariantCode, variantCode);
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public List<SaleModelVariantPolicyPo> findBySaleModelCode(String saleModelCode) {
        LambdaQueryWrapper<SaleModelVariantPolicyPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelVariantPolicyPo::getSaleModelCode, saleModelCode);
        return mapper.selectList(wrapper);
    }

    @Override
    public List<SaleModelVariantPolicyPo> findBySaleModelCodeAndModelCode(String saleModelCode, String modelCode) {
        // 需要关联查询 MDM 投影表来获取 modelCode 对应的 variantCode
        // 这里暂时返回空列表，实际实现需要根据业务逻辑调整
        return List.of();
    }

    @Override
    public int insert(SaleModelVariantPolicyPo entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(SaleModelVariantPolicyPo entity) {
        LambdaUpdateWrapper<SaleModelVariantPolicyPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SaleModelVariantPolicyPo::getId, entity.getId())
               .set(SaleModelVariantPolicyPo::getSaleStatus, entity.getSaleStatus())
               .set(SaleModelVariantPolicyPo::getVariantPrice, entity.getVariantPrice())
               .set(SaleModelVariantPolicyPo::getEarnestMoneyPrice, entity.getEarnestMoneyPrice())
               .set(SaleModelVariantPolicyPo::getDownPaymentPrice, entity.getDownPaymentPrice())
               .set(SaleModelVariantPolicyPo::getAvailableRegions, entity.getAvailableRegions())
               .set(SaleModelVariantPolicyPo::getChannels, entity.getChannels())
               .set(SaleModelVariantPolicyPo::getMarketingName, entity.getMarketingName())
               .set(SaleModelVariantPolicyPo::getMarketingImage, entity.getMarketingImage())
               .set(SaleModelVariantPolicyPo::getMarketingCopy, entity.getMarketingCopy())
               .set(SaleModelVariantPolicyPo::getSortWeight, entity.getSortWeight())
               .set(SaleModelVariantPolicyPo::getEffectiveFrom, entity.getEffectiveFrom())
               .set(SaleModelVariantPolicyPo::getEffectiveTo, entity.getEffectiveTo())
               .set(SaleModelVariantPolicyPo::getDescription, entity.getDescription())
               .set(SaleModelVariantPolicyPo::getModifyTime, entity.getModifyTime())
               .set(SaleModelVariantPolicyPo::getModifyBy, entity.getModifyBy());
        return mapper.update(null, wrapper);
    }

    @Override
    public int deleteById(Long id) {
        return mapper.deleteById(id);
    }
}
