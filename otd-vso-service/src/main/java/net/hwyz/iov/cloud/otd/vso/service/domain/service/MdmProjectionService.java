package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.MdmProjectionStaleException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.MdmProjectionRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * MDM 投影服务
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MdmProjectionService {

    private final MdmProjectionRepository projectionRepository;

    /**
     * 获取 Carline 投影
     */
    public MdmProjectionCarlinePo getCarline(String carlineCode) {
        return projectionRepository.findCarlineByCode(carlineCode)
            .orElseThrow(() -> new MdmProjectionStaleException(
                String.format("Carline [%s] 在本地投影中不存在", carlineCode)));
    }

    /**
     * 获取 Carline 投影（可选）
     */
    public Optional<MdmProjectionCarlinePo> getCarlineOptional(String carlineCode) {
        return projectionRepository.findCarlineByCode(carlineCode);
    }

    /**
     * 获取所有 Carline 投影
     */
    public List<MdmProjectionCarlinePo> getAllCarlines() {
        return projectionRepository.findAllCarlines();
    }

    /**
     * 保存或更新 Carline 投影
     */
    public void saveOrUpdateCarline(MdmProjectionCarlinePo po) {
        Optional<MdmProjectionCarlinePo> existing = projectionRepository.findCarlineByCode(po.getCarlineCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateCarline(po);
            log.info("更新 Carline 投影: {}", po.getCarlineCode());
        } else {
            projectionRepository.saveCarline(po);
            log.info("新增 Carline 投影: {}", po.getCarlineCode());
        }
    }

    /**
     * 获取 Model 投影
     */
    public MdmProjectionModelPo getModel(String modelCode) {
        return projectionRepository.findModelByCode(modelCode)
            .orElseThrow(() -> new MdmProjectionStaleException(
                String.format("Model [%s] 在本地投影中不存在", modelCode)));
    }

    /**
     * 获取 Model 投影（可选）
     */
    public Optional<MdmProjectionModelPo> getModelOptional(String modelCode) {
        return projectionRepository.findModelByCode(modelCode);
    }

    /**
     * 获取 Carline 下的所有 Model
     */
    public List<MdmProjectionModelPo> getModelsByCarlineCode(String carlineCode) {
        return projectionRepository.findModelsByCarlineCode(carlineCode);
    }

    /**
     * 保存或更新 Model 投影
     */
    public void saveOrUpdateModel(MdmProjectionModelPo po) {
        Optional<MdmProjectionModelPo> existing = projectionRepository.findModelByCode(po.getModelCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateModel(po);
            log.info("更新 Model 投影: {}", po.getModelCode());
        } else {
            projectionRepository.saveModel(po);
            log.info("新增 Model 投影: {}", po.getModelCode());
        }
    }

    /**
     * 获取 Variant 投影
     */
    public MdmProjectionVariantPo getVariant(String variantCode) {
        return projectionRepository.findVariantByCode(variantCode)
            .orElseThrow(() -> new MdmProjectionStaleException(
                String.format("Variant [%s] 在本地投影中不存在", variantCode)));
    }

    /**
     * 获取 Variant 投影（可选）
     */
    public Optional<MdmProjectionVariantPo> getVariantOptional(String variantCode) {
        return projectionRepository.findVariantByCode(variantCode);
    }

    /**
     * 获取 Configuration 投影
     */
    public MdmProjectionConfigurationPo getConfiguration(String configurationCode) {
        return projectionRepository.findConfigurationByCode(configurationCode)
            .orElseThrow(() -> new MdmProjectionStaleException(
                String.format("Configuration [%s] 在本地投影中不存在", configurationCode)));
    }

    /**
     * 获取 Configuration 投影（可选）
     */
    public Optional<MdmProjectionConfigurationPo> getConfigurationOptional(String configurationCode) {
        return projectionRepository.findConfigurationByCode(configurationCode);
    }

    /**
     * 获取 Variant 下的所有 Configuration
     */
    public List<MdmProjectionConfigurationPo> getConfigurationsByVariant(String variantCode) {
        return projectionRepository.findConfigurationsByVariantCode(variantCode);
    }

    /**
     * 获取 OptionCode 投影
     */
    public MdmProjectionOptionPo getOption(String optionCode) {
        return projectionRepository.findOptionByCode(optionCode)
            .orElseThrow(() -> new MdmProjectionStaleException(
                String.format("OptionCode [%s] 在本地投影中不存在", optionCode)));
    }

    /**
     * 获取 OptionCode 投影（可选）
     */
    public Optional<MdmProjectionOptionPo> getOptionOptional(String optionCode) {
        return projectionRepository.findOptionByCode(optionCode);
    }

    /**
     * 获取 OptionFamily 下的所有 OptionCode
     */
    public List<MdmProjectionOptionPo> getOptionsByOptionFamily(String optionFamilyCode) {
        return projectionRepository.findOptionsByOptionFamilyCode(optionFamilyCode);
    }

    /**
     * 获取所有 OptionFamily
     */
    public List<MdmProjectionOptionFamilyPo> getAllOptionFamilies() {
        return projectionRepository.findAllOptionFamilies();
    }

    /**
     * 获取 OptionFamily 投影（可选）
     */
    public Optional<MdmProjectionOptionFamilyPo> getOptionFamilyOptional(String optionFamilyCode) {
        return projectionRepository.findOptionFamilyByCode(optionFamilyCode);
    }

    /**
     * 保存或更新 Variant 投影
     */
    public void saveOrUpdateVariant(MdmProjectionVariantPo po) {
        Optional<MdmProjectionVariantPo> existing = projectionRepository.findVariantByCode(po.getVariantCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateVariant(po);
            log.info("更新 Variant 投影: {}", po.getVariantCode());
        } else {
            projectionRepository.saveVariant(po);
            log.info("新增 Variant 投影: {}", po.getVariantCode());
        }
    }

    /**
     * 保存或更新 Configuration 投影
     */
    public void saveOrUpdateConfiguration(MdmProjectionConfigurationPo po) {
        Optional<MdmProjectionConfigurationPo> existing = projectionRepository.findConfigurationByCode(po.getConfigurationCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateConfiguration(po);
            log.info("更新 Configuration 投影: {}", po.getConfigurationCode());
        } else {
            projectionRepository.saveConfiguration(po);
            log.info("新增 Configuration 投影: {}", po.getConfigurationCode());
        }
    }

    /**
     * 保存或更新 OptionCode 投影
     */
    public void saveOrUpdateOption(MdmProjectionOptionPo po) {
        Optional<MdmProjectionOptionPo> existing = projectionRepository.findOptionByCode(po.getOptionCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateOption(po);
            log.info("更新 OptionCode 投影: {}", po.getOptionCode());
        } else {
            projectionRepository.saveOption(po);
            log.info("新增 OptionCode 投影: {}", po.getOptionCode());
        }
    }

    /**
     * 保存或更新 OptionFamily 投影
     */
    public void saveOrUpdateOptionFamily(MdmProjectionOptionFamilyPo po) {
        Optional<MdmProjectionOptionFamilyPo> existing = projectionRepository.findOptionFamilyByCode(po.getOptionFamilyCode());
        if (existing.isPresent()) {
            po.setId(existing.get().getId());
            projectionRepository.updateOptionFamily(po);
            log.info("更新 OptionFamily 投影: {}", po.getOptionFamilyCode());
        } else {
            projectionRepository.saveOptionFamily(po);
            log.info("新增 OptionFamily 投影: {}", po.getOptionFamilyCode());
        }
    }
}
