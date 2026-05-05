package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBuildConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SaleModelBuildConfigMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaleModelBuildConfigRepositoryImpl implements SaleModelBuildConfigRepository {

    private final SaleModelBuildConfigMapper mapper;

    @Override
    public Optional<SaleModelBuildConfigPo> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    @Override
    public List<SaleModelBuildConfigPo> findBySaleCode(String saleCode) {
        LambdaQueryWrapper<SaleModelBuildConfigPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelBuildConfigPo::getSaleCode, saleCode)
                .orderByAsc(SaleModelBuildConfigPo::getSort);
        return mapper.selectList(wrapper);
    }

    @Override
    public Optional<SaleModelBuildConfigPo> findBySaleCodeAndBuildConfigCode(String saleCode, String buildConfigCode) {
        LambdaQueryWrapper<SaleModelBuildConfigPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelBuildConfigPo::getSaleCode, saleCode)
                .eq(SaleModelBuildConfigPo::getBuildConfigCode, buildConfigCode);
        return Optional.ofNullable(mapper.selectOne(wrapper));
    }

    @Override
    public int insert(SaleModelBuildConfigPo entity) {
        return mapper.insert(entity);
    }

    @Override
    public int update(SaleModelBuildConfigPo entity) {
        return mapper.updateById(entity);
    }

    @Override
    public int physicalDeleteByIds(Long[] ids) {
        return mapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public int physicalDeleteBySaleCode(String saleCode) {
        LambdaQueryWrapper<SaleModelBuildConfigPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleModelBuildConfigPo::getSaleCode, saleCode);
        return mapper.delete(wrapper);
    }
}