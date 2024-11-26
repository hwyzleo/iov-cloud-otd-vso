package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.enums.Symbol;
import net.hwyz.iov.cloud.otd.vso.api.contract.*;
import net.hwyz.iov.cloud.otd.vso.api.contract.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.domain.external.service.ExDictionaryService;
import net.hwyz.iov.cloud.otd.vso.service.domain.external.service.ExVehicleModelConfigService;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.PurchaseAgreementAssembler;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.PurchaseBenefitsAssembler;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelAssembler;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelConfigAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.ModelConfigCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.PurchaseAgreementDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.PurchaseBenefitsDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.SaleModelConfigDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.SaleModelDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseAgreementPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.PurchaseBenefitsPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 销售车型应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleModelAppService {

    private final SaleModelDao saleModelDao;
    private final SaleModelConfigDao saleModelConfigDao;
    private final ExDictionaryService exDictionaryService;
    private final PurchaseBenefitsDao purchaseBenefitsDao;
    private final PurchaseAgreementDao purchaseAgreementDao;
    private final ExVehicleModelConfigService exVehicleModelConfigService;

    /**
     * 获取销售车型列表
     *
     * @return 销售车型列表
     */
    public List<SaleModel> getSaleModelList() {
        List<SaleModelPo> saleModelPoList = saleModelDao.selectPoByExample(SaleModelPo.builder().build());
        return SaleModelAssembler.INSTANCE.fromPoList(saleModelPoList);
    }

    /**
     * 获取销售车型信息
     *
     * @param saleCode 销售代码
     * @return 销售车型信息
     */
    public SaleModel getSaleModel(String saleCode) {
        List<SaleModelPo> saleModelPoList = saleModelDao.selectPoByExample(SaleModelPo.builder().saleCode(saleCode).build());
        if (saleModelPoList.isEmpty()) {
            return null;
        }
        return SaleModelAssembler.INSTANCE.fromPo(saleModelPoList.get(0));
    }

    /**
     * 获取销售车型列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    public List<SaleModelConfig> getSaleModelResponse(String saleCode) {
        return SaleModelConfigAssembler.INSTANCE.fromPoList(getSaleModelConfigList(saleCode));
    }

    /**
     * 获取选中的销售车型信息
     *
     * @param saleCode      销售代码
     * @param modelCode     车型代码
     * @param exteriorCode  外饰代码
     * @param interiorCode  内饰代码
     * @param wheelCode     车轮代码
     * @param spareTireCode 备胎代码
     * @param adasCode      智驾代码
     * @return 选中的销售车型信息
     */
    public SelectedSaleModel getSelectedSaleModel(String saleCode, String modelCode, String exteriorCode, String interiorCode,
                                                  String wheelCode, String spareTireCode, String adasCode) {
        List<SaleModelPo> saleModelPoList = saleModelDao.selectPoByExample(SaleModelPo.builder().saleCode(saleCode).build());
        if (saleModelPoList.isEmpty()) {
            throw new SaleModelNotExistException(saleCode);
        }
        SaleModelPo saleModelPo = saleModelPoList.get(0);
        Map<String, String> saleModelConfigType = new HashMap<>();
        saleModelConfigType.put(SaleModelConfigType.MODEL.name(), modelCode);
        saleModelConfigType.put(SaleModelConfigType.EXTERIOR.name(), exteriorCode);
        saleModelConfigType.put(SaleModelConfigType.INTERIOR.name(), interiorCode);
        saleModelConfigType.put(SaleModelConfigType.WHEEL.name(), wheelCode);
        saleModelConfigType.put(SaleModelConfigType.SPARE_TIRE.name(), spareTireCode);
        saleModelConfigType.put(SaleModelConfigType.ADAS.name(), adasCode);
        List<String> modelImages = new ArrayList<>();
        String modelName = "";
        StringBuilder modelDesc = new StringBuilder();
        Map<String, SaleModelConfigPo> saleModelConfigMap = getSaleModelConfigMap(saleCode);
        Map<String, String> modelConfigName = new LinkedHashMap<>();
        Map<String, BigDecimal> modelConfigPrice = new LinkedHashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        SaleModelConfigPo modelConfig = saleModelConfigMap.get(SaleModelConfigType.MODEL.name() + Symbol.UNDERSCORE.value + modelCode);
        if (modelConfig != null) {
            modelName = modelConfig.getTypeName();
            modelConfigName.put(SaleModelConfigType.MODEL.name(), modelConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.MODEL.name(), modelConfig.getTypePrice());
            totalPrice = totalPrice.add(modelConfig.getTypePrice());
        }
        SaleModelConfigPo spareTireConfig = saleModelConfigMap.get(SaleModelConfigType.SPARE_TIRE.name() + Symbol.UNDERSCORE.value + spareTireCode);
        if (spareTireConfig != null) {
            if (StrUtil.isNotBlank(spareTireConfig.getTypeName())) {
                modelDesc.append(spareTireConfig.getTypeName());
            }
            modelConfigName.put(SaleModelConfigType.SPARE_TIRE.name(), spareTireConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.SPARE_TIRE.name(), spareTireConfig.getTypePrice());
            totalPrice = totalPrice.add(spareTireConfig.getTypePrice());
        }
        SaleModelConfigPo exteriorConfig = saleModelConfigMap.get(SaleModelConfigType.EXTERIOR.name() + Symbol.UNDERSCORE.value + exteriorCode);
        if (exteriorConfig != null) {
            if (StrUtil.isNotBlank(exteriorConfig.getTypeName())) {
                if (modelDesc.length() > 0) {
                    modelDesc.append(" | ");
                }
                modelDesc.append(exteriorConfig.getTypeName());
            }
            modelConfigName.put(SaleModelConfigType.EXTERIOR.name(), exteriorConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.EXTERIOR.name(), exteriorConfig.getTypePrice());
            totalPrice = totalPrice.add(exteriorConfig.getTypePrice());
            if (StrUtil.isNotBlank(exteriorConfig.getTypeImage())) {
                List<String> list = JSONUtil.toBean(exteriorConfig.getTypeImage(), new TypeReference<>() {
                }, true);
                if (!list.isEmpty()) {
                    modelImages.add(list.get(0));
                }
            }
        }
        SaleModelConfigPo wheelConfig = saleModelConfigMap.get(SaleModelConfigType.WHEEL.name() + Symbol.UNDERSCORE.value + wheelCode);
        if (wheelConfig != null) {
            if (StrUtil.isNotBlank(wheelConfig.getTypeName())) {
                if (modelDesc.length() > 0) {
                    modelDesc.append(" | ");
                }
                modelDesc.append(wheelConfig.getTypeName());
            }
            modelConfigName.put(SaleModelConfigType.WHEEL.name(), wheelConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.WHEEL.name(), wheelConfig.getTypePrice());
            totalPrice = totalPrice.add(wheelConfig.getTypePrice());
        }
        SaleModelConfigPo interiorConfig = saleModelConfigMap.get(SaleModelConfigType.INTERIOR.name() + Symbol.UNDERSCORE.value + interiorCode);
        if (interiorConfig != null) {
            if (StrUtil.isNotBlank(interiorConfig.getTypeName())) {
                if (modelDesc.length() > 0) {
                    modelDesc.append(" | ");
                }
                modelDesc.append(interiorConfig.getTypeName());
            }
            modelConfigName.put(SaleModelConfigType.INTERIOR.name(), interiorConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.INTERIOR.name(), interiorConfig.getTypePrice());
            totalPrice = totalPrice.add(interiorConfig.getTypePrice());
            if (StrUtil.isNotBlank(interiorConfig.getTypeImage())) {
                List<String> list = JSONUtil.toBean(interiorConfig.getTypeImage(), new TypeReference<>() {
                }, true);
                if (!list.isEmpty()) {
                    modelImages.add(list.get(0));
                }
            }
        }
        SaleModelConfigPo adasConfig = saleModelConfigMap.get(SaleModelConfigType.ADAS.name() + Symbol.UNDERSCORE.value + adasCode);
        if (adasConfig != null) {
            if (StrUtil.isNotBlank(adasConfig.getTypeName())) {
                if (modelDesc.length() > 0) {
                    modelDesc.append(" | ");
                }
                modelDesc.append(adasConfig.getTypeName());
            }
            modelConfigName.put(SaleModelConfigType.ADAS.name(), adasConfig.getTypeName());
            modelConfigPrice.put(SaleModelConfigType.ADAS.name(), adasConfig.getTypePrice());
            totalPrice = totalPrice.add(adasConfig.getTypePrice());
        }
        String purchaseBenefitsIntro = "";
        PurchaseBenefits purchaseBenefits = getPurchaseBenefits(saleCode);
        if (purchaseBenefits != null) {
            purchaseBenefitsIntro = purchaseBenefits.getIntro();
        }
        return SelectedSaleModel.builder()
                .saleCode(saleCode)
                .earnestMoney(saleModelPo.getEarnestMoney())
                .earnestMoneyPrice(saleModelPo.getEarnestMoneyPrice())
                .downPayment(saleModelPo.getDownPayment())
                .downPaymentPrice(saleModelPo.getDownPaymentPrice())
                .modelConfigCode(getModelConfigCode(saleModelConfigType))
                .saleModelImages(modelImages)
                .modelName(modelName)
                .saleModelDesc(modelDesc.toString())
                .saleModelConfigName(modelConfigName)
                .saleModelConfigPrice(modelConfigPrice)
                .totalPrice(totalPrice)
                .purchaseBenefitsIntro(purchaseBenefitsIntro)
                .build();
    }

    /**
     * 获取销售车型Map
     *
     * @param saleCode 销售代码
     * @return 销售车型Map key:销售车型类型_销售车型类型代码 value:销售车型Po
     */
    public Map<String, SaleModelConfigPo> getSaleModelConfigMap(String saleCode) {
        return getSaleModelConfigList(saleCode).stream().collect(Collectors.toMap(k -> k.getType() + Symbol.UNDERSCORE.value + k.getTypeCode(), v -> v));
    }

    /**
     * 获取销售车型购车权益
     *
     * @param saleCode 销售代码
     * @return 销售车型购车权益
     */
    public PurchaseBenefits getPurchaseBenefits(String saleCode) {
        PurchaseBenefitsPo purchaseBenefitsPo = purchaseBenefitsDao.selectCurrentPoBySaleCode(saleCode);
        return PurchaseBenefitsAssembler.INSTANCE.fromPo(purchaseBenefitsPo);
    }

    /**
     * 获取销售车型购车协议
     *
     * @param saleCode 销售代码
     * @return 销售车型购车协议
     */
    public PurchaseAgreement getPurchaseAgreement(String saleCode, Integer type) {
        List<PurchaseAgreementPo> purchaseAgreementPoList = purchaseAgreementDao.selectPoByExample(PurchaseAgreementPo.builder()
                .saleCode(saleCode)
                .type(type)
                .build());
        if (purchaseAgreementPoList.isEmpty()) {
            return null;
        }
        return PurchaseAgreementAssembler.INSTANCE.fromPo(purchaseAgreementPoList.get(0));
    }

    /**
     * 获取车型配置代码
     *
     * @param saleModelConfigType 销售车型配置类型
     * @return 车型配置代码
     */
    public String getModelConfigCode(Map<String, String> saleModelConfigType) {
        String modelCode = saleModelConfigType.get(SaleModelConfigType.MODEL.name());
        String exteriorCode = saleModelConfigType.get(SaleModelConfigType.EXTERIOR.name());
        String interiorCode = saleModelConfigType.get(SaleModelConfigType.INTERIOR.name());
        String wheelCode = saleModelConfigType.get(SaleModelConfigType.WHEEL.name());
        String spareTireCode = saleModelConfigType.get(SaleModelConfigType.SPARE_TIRE.name());
        String adasCode = saleModelConfigType.get(SaleModelConfigType.ADAS.name());
        String vehicleModeConfigCode = exVehicleModelConfigService.getVehicleModeConfigCode(modelCode, exteriorCode,
                interiorCode, wheelCode, spareTireCode, adasCode);
        if (vehicleModeConfigCode == null) {
            throw new ModelConfigCodeNotExistException(modelCode, exteriorCode, interiorCode, wheelCode, spareTireCode, adasCode);
        }
        return vehicleModeConfigCode;
    }

    /**
     * 获取上牌区域列表
     *
     * @return 销售区域列表
     */
    public List<LicenseArea> getLicenseAreaList() {
        List<LicenseArea> list = new ArrayList<>();
        exDictionaryService.getDictionaryMap("province").forEach(province ->
                list.add(LicenseArea.builder()
                        .provinceCode(province.get("code").toString())
                        .displayName(province.get("name").toString())
                        .build()));
        exDictionaryService.getDictionaryMap("city").forEach(city ->
                list.add(LicenseArea.builder()
                        .provinceCode(city.get("province_code").toString())
                        .cityCode(city.get("code").toString())
                        .displayName(city.get("name").toString())
                        .build()));
        return list;
    }

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode 销售代码
     * @return 销售车型列表
     */
    private List<SaleModelConfigPo> getSaleModelConfigList(String saleCode) {
        return saleModelConfigDao.selectPoByExample(SaleModelConfigPo.builder().saleCode(saleCode).build());
    }

}
