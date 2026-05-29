package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.MdmProjectionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionVariantMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionConfigurationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionOptionMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MdmProjectionRepositoryImpl implements MdmProjectionRepository {
    private final MdmProjectionVariantMapper variantMapper;
    private final MdmProjectionConfigurationMapper configurationMapper;
    private final MdmProjectionOptionMapper optionMapper;

    @Override
    public Optional<MdmProjectionVariantPo> findVariantByCode(String variantCode) {
        return Optional.ofNullable(variantMapper.selectByVariantCode(variantCode));
    }

    @Override
    public void saveVariant(MdmProjectionVariantPo po) {
        variantMapper.insert(po);
    }

    @Override
    public void updateVariant(MdmProjectionVariantPo po) {
        variantMapper.updateById(po);
    }

    @Override
    public Optional<MdmProjectionConfigurationPo> findConfigurationByCode(String configurationCode) {
        return Optional.ofNullable(configurationMapper.selectByConfigurationCode(configurationCode));
    }

    @Override
    public List<MdmProjectionConfigurationPo> findConfigurationsByVariantCode(String variantCode) {
        return configurationMapper.selectByVariantCode(variantCode);
    }

    @Override
    public void saveConfiguration(MdmProjectionConfigurationPo po) {
        configurationMapper.insert(po);
    }

    @Override
    public void updateConfiguration(MdmProjectionConfigurationPo po) {
        configurationMapper.updateById(po);
    }

    @Override
    public Optional<MdmProjectionOptionPo> findOptionByCode(String optionCode) {
        return Optional.ofNullable(optionMapper.selectByOptionCode(optionCode));
    }

    @Override
    public void saveOption(MdmProjectionOptionPo po) {
        optionMapper.insert(po);
    }

    @Override
    public void updateOption(MdmProjectionOptionPo po) {
        optionMapper.updateById(po);
    }
}
