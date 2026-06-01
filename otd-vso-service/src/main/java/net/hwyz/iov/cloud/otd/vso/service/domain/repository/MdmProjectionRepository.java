package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionCarlinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import java.util.List;
import java.util.Optional;

public interface MdmProjectionRepository {
    // Carline 投影
    Optional<MdmProjectionCarlinePo> findCarlineByCode(String carlineCode);
    List<MdmProjectionCarlinePo> findAllCarlines();
    void saveCarline(MdmProjectionCarlinePo po);
    void updateCarline(MdmProjectionCarlinePo po);

    // Model 投影
    Optional<MdmProjectionModelPo> findModelByCode(String modelCode);
    List<MdmProjectionModelPo> findModelsByCarlineCode(String carlineCode);
    void saveModel(MdmProjectionModelPo po);
    void updateModel(MdmProjectionModelPo po);

    // Variant 投影
    Optional<MdmProjectionVariantPo> findVariantByCode(String variantCode);
    void saveVariant(MdmProjectionVariantPo po);
    void updateVariant(MdmProjectionVariantPo po);

    // Configuration 投影
    Optional<MdmProjectionConfigurationPo> findConfigurationByCode(String configurationCode);
    List<MdmProjectionConfigurationPo> findConfigurationsByVariantCode(String variantCode);
    void saveConfiguration(MdmProjectionConfigurationPo po);
    void updateConfiguration(MdmProjectionConfigurationPo po);

    // Option 投影
    Optional<MdmProjectionOptionPo> findOptionByCode(String optionCode);
    List<MdmProjectionOptionPo> findOptionsByOptionFamilyCode(String optionFamilyCode);
    void saveOption(MdmProjectionOptionPo po);
    void updateOption(MdmProjectionOptionPo po);

    // OptionFamily 投影
    Optional<MdmProjectionOptionFamilyPo> findOptionFamilyByCode(String optionFamilyCode);
    List<MdmProjectionOptionFamilyPo> findAllOptionFamilies();
    void saveOptionFamily(MdmProjectionOptionFamilyPo po);
    void updateOptionFamily(MdmProjectionOptionFamilyPo po);
}
