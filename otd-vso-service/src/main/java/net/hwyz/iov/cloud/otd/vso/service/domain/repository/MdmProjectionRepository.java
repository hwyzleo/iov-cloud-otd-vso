package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionVariantPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionConfigurationPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.MdmProjectionOptionFamilyPo;
import java.util.List;
import java.util.Optional;

public interface MdmProjectionRepository {
    Optional<MdmProjectionVariantPo> findVariantByCode(String variantCode);
    void saveVariant(MdmProjectionVariantPo po);
    void updateVariant(MdmProjectionVariantPo po);

    Optional<MdmProjectionConfigurationPo> findConfigurationByCode(String configurationCode);
    List<MdmProjectionConfigurationPo> findConfigurationsByVariantCode(String variantCode);
    void saveConfiguration(MdmProjectionConfigurationPo po);
    void updateConfiguration(MdmProjectionConfigurationPo po);

    Optional<MdmProjectionOptionPo> findOptionByCode(String optionCode);
    List<MdmProjectionOptionPo> findOptionsByOptionFamilyCode(String optionFamilyCode);
    void saveOption(MdmProjectionOptionPo po);
    void updateOption(MdmProjectionOptionPo po);

    Optional<MdmProjectionOptionFamilyPo> findOptionFamilyByCode(String optionFamilyCode);
    List<MdmProjectionOptionFamilyPo> findAllOptionFamilies();
    void saveOptionFamily(MdmProjectionOptionFamilyPo po);
    void updateOptionFamily(MdmProjectionOptionFamilyPo po);
}
