package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.MdmProjectionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionCarlineMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionModelMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionVariantMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionConfigurationMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionOptionMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.MdmProjectionOptionFamilyMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MdmProjectionRepositoryImpl implements MdmProjectionRepository {
    private final MdmProjectionCarlineMapper carlineMapper;
    private final MdmProjectionModelMapper modelMapper;
    private final MdmProjectionVariantMapper variantMapper;
    private final MdmProjectionConfigurationMapper configurationMapper;
    private final MdmProjectionOptionMapper optionMapper;
    private final MdmProjectionOptionFamilyMapper optionFamilyMapper;

    // Carline 投影
    @Override
    public Optional<MdmProjectionCarlinePo> findCarlineByCode(String carlineCode) {
        return Optional.ofNullable(carlineMapper.selectByCarlineCode(carlineCode));
    }

    @Override
    public List<MdmProjectionCarlinePo> findAllCarlines() {
        return carlineMapper.selectList(null);
    }

    @Override
    public void saveCarline(MdmProjectionCarlinePo po) {
        carlineMapper.insert(po);
    }

    @Override
    public void updateCarline(MdmProjectionCarlinePo po) {
        carlineMapper.updateById(po);
    }

    // Model 投影
    @Override
    public Optional<MdmProjectionModelPo> findModelByCode(String modelCode) {
        return Optional.ofNullable(modelMapper.selectByModelCode(modelCode));
    }

    @Override
    public List<MdmProjectionModelPo> findModelsByCarlineCode(String carlineCode) {
        return modelMapper.selectByCarlineCode(carlineCode);
    }

    @Override
    public void saveModel(MdmProjectionModelPo po) {
        modelMapper.insert(po);
    }

    @Override
    public void updateModel(MdmProjectionModelPo po) {
        modelMapper.updateById(po);
    }

    // Variant 投影
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

    // Configuration 投影
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

    // Option 投影
    @Override
    public Optional<MdmProjectionOptionPo> findOptionByCode(String optionCode) {
        return Optional.ofNullable(optionMapper.selectByOptionCode(optionCode));
    }

    @Override
    public List<MdmProjectionOptionPo> findOptionsByOptionFamilyCode(String optionFamilyCode) {
        return optionMapper.selectByOptionFamilyCode(optionFamilyCode);
    }

    @Override
    public void saveOption(MdmProjectionOptionPo po) {
        optionMapper.insert(po);
    }

    @Override
    public void updateOption(MdmProjectionOptionPo po) {
        optionMapper.updateById(po);
    }

    // OptionFamily 投影
    @Override
    public Optional<MdmProjectionOptionFamilyPo> findOptionFamilyByCode(String optionFamilyCode) {
        return Optional.ofNullable(optionFamilyMapper.selectByOptionFamilyCode(optionFamilyCode));
    }

    @Override
    public List<MdmProjectionOptionFamilyPo> findAllOptionFamilies() {
        return optionFamilyMapper.selectList(null);
    }

    @Override
    public void saveOptionFamily(MdmProjectionOptionFamilyPo po) {
        optionFamilyMapper.insert(po);
    }

    @Override
    public void updateOptionFamily(MdmProjectionOptionFamilyPo po) {
        optionFamilyMapper.updateById(po);
    }
}
