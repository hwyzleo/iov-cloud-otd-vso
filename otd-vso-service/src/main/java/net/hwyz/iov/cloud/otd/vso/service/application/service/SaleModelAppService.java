package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigFeatureCodeResponse;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.framework.common.enums.Symbol;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelResultAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import net.hwyz.iov.cloud.otd.vso.api.enums.SaleModelConfigType;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BuildConfigCodeNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBuildConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseAgreementMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseBenefitsMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelBuildConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseAgreementPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PurchaseBenefitsPo;

import java.util.Arrays;
import net.hwyz.iov.cloud.tsp.dictionary.api.feign.service.ExDictionaryService;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleModelAppService {

    private final SaleModelRepository saleModelRepository;
    private final SaleModelConfigRepository saleModelConfigRepository;
    private final SaleModelBuildConfigRepository saleModelBuildConfigRepository;
    private final ExDictionaryService exDictionaryService;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;
    private final PurchaseBenefitsMapper purchaseBenefitsMapper;
    private final PurchaseAgreementMapper purchaseAgreementMapper;

    public List<SaleModelResult> search(SaleModelQuery query) {
        List<SaleModelPo> poList = saleModelRepository.findByCondition(query);
        return PageUtil.convert(poList, SaleModelResultAssembler.INSTANCE::toResult);
    }

    public List<SaleModelResult> getSaleModelList() {
        return SaleModelResultAssembler.INSTANCE.toResultList(saleModelRepository.findAll());
    }

    public SaleModelResult getSaleModelById(Long id) {
        return saleModelRepository.findById(id)
                .map(SaleModelResultAssembler.INSTANCE::toResult)
                .orElseThrow(() -> new SaleModelNotExistException(""));
    }

    public SaleModelResult getSaleModelByCode(String saleCode) {
        return saleModelRepository.findBySaleCode(saleCode)
                .map(SaleModelResultAssembler.INSTANCE::toResult)
                .orElseThrow(() -> new SaleModelNotExistException(saleCode));
    }

    public boolean checkSaleCodeUnique(Long saleModelId, String saleCode) {
        return !saleModelRepository.existsBySaleCodeExcludeId(saleCode, saleModelId);
    }

    public List<SaleModelConfigResult> getSaleModelConfigList(Long saleModelId) {
        return SaleModelResultAssembler.INSTANCE.toConfigResultList(saleModelConfigRepository.findBySaleModelId(saleModelId));
    }

    public List<SaleModelConfigResult> getSaleModelConfigList(String saleCode) {
        return SaleModelResultAssembler.INSTANCE.toConfigResultList(saleModelConfigRepository.findBySaleCode(saleCode));
    }

    public SaleModelConfigResult getSaleModelConfigById(Long saleModelId, Long saleModelConfigId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        return saleModelConfigRepository.findByIdAndSaleCode(saleModelConfigId, model.getSaleCode())
                .map(SaleModelResultAssembler.INSTANCE::toConfigResult)
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSaleModel(SaleModelCreateDto dto, String userId) {
        if (!checkSaleCodeUnique(null, dto.getSaleCode())) {
            throw new IllegalArgumentException("销售代码已存在：" + dto.getSaleCode());
        }
        SaleModelPo entity = SaleModelPoAssembler.INSTANCE.toDo(dto);
        entity.setCreateBy(userId);
        entity.setModifyBy(userId);
        saleModelRepository.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSaleModelConfig(Long saleModelId, SaleModelConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        SaleModelConfigPo entity = SaleModelPoAssembler.INSTANCE.toConfigDo(dto);
        entity.setSaleCode(model.getSaleCode());
        entity.setCreateBy(userId);
        entity.setModifyBy(userId);
        saleModelConfigRepository.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySaleModel(SaleModelUpdateDto dto, String userId) {
        if (!checkSaleCodeUnique(dto.getId(), dto.getSaleCode())) {
            throw new IllegalArgumentException("销售代码已存在：" + dto.getSaleCode());
        }
        SaleModelPo entity = SaleModelPoAssembler.INSTANCE.toUpdateDo(dto);
        entity.setModifyBy(userId);
        saleModelRepository.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySaleModelConfig(Long saleModelId, SaleModelConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        SaleModelConfigPo entity = SaleModelPoAssembler.INSTANCE.toConfigDo(dto);
        entity.setId(dto.getId());
        entity.setSaleCode(model.getSaleCode());
        entity.setModifyBy(userId);
        saleModelConfigRepository.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySaleModelImages(String saleCode, List<String> images, String userId) {
        SaleModelResult result = getSaleModelByCode(saleCode);
        SaleModelPo entity = saleModelRepository.findById(result.getId()).orElseThrow();
        entity.setImages(cn.hutool.json.JSONUtil.toJsonStr(images));
        entity.setModifyBy(userId);
        saleModelRepository.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleModelByIds(Long[] ids) {
        for (Long id : ids) {
            SaleModelResult model = getSaleModelById(id);
            List<SaleModelConfigPo> configs = saleModelConfigRepository.findBySaleCode(model.getSaleCode());
            if (!configs.isEmpty()) {
                Long[] configIds = configs.stream().map(SaleModelConfigPo::getId).toArray(Long[]::new);
                saleModelConfigRepository.physicalDeleteBySaleCodeAndIds(model.getSaleCode(), configIds);
            }
        }
        saleModelRepository.physicalDeleteByIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleModelConfigByIds(Long saleModelId, Long[] ids) {
        if (ids == null || ids.length == 0) return;
        SaleModelResult model = getSaleModelById(saleModelId);
        saleModelConfigRepository.physicalDeleteBySaleCodeAndIds(model.getSaleCode(), ids);
    }

    public Map<String, SaleModelConfigResult> getSaleModelConfigMap(String saleCode) {
        return getSaleModelConfigList(saleCode).stream()
                .collect(Collectors.toMap(
                        k -> k.getType() + Symbol.UNDERSCORE.value + k.getTypeCode(),
                        v -> v,
                        (v1, v2) -> v1));
    }

    public SelectedSaleModelResult getSelectedSaleModel(String saleCode, String modelCode, String exteriorCode,
                                                   String interiorCode, String wheelCode, String spareTireCode,
                                                   String adasCode) {
        List<SaleModelPo> list = saleModelRepository.findByCondition(SaleModelQuery.builder()
                .saleCode(saleCode)
                .build());
        if (list.isEmpty()) throw new SaleModelNotExistException(saleCode);
        SaleModelPo model = list.get(0);

        Map<String, String> configType = new HashMap<>();
        configType.put(SaleModelConfigType.MODEL.name(), modelCode);
        configType.put(SaleModelConfigType.EXTERIOR.name(), exteriorCode);
        configType.put(SaleModelConfigType.INTERIOR.name(), interiorCode);
        configType.put(SaleModelConfigType.WHEEL.name(), wheelCode);
        configType.put(SaleModelConfigType.SPARE_TIRE.name(), spareTireCode);
        configType.put(SaleModelConfigType.ADAS.name(), adasCode);

        List<String> images = new ArrayList<>();
        String modelName = "";
        StringBuilder desc = new StringBuilder();
        Map<String, SaleModelConfigResult> configMap = getSaleModelConfigMap(saleCode);
        Map<String, String> configName = new LinkedHashMap<>();
        Map<String, BigDecimal> configPrice = new LinkedHashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        SaleModelConfigResult mc = configMap.get(SaleModelConfigType.MODEL.name() + "_" + modelCode);
        if (mc != null) {
            modelName = mc.getTypeName();
            configName.put(SaleModelConfigType.MODEL.name(), mc.getTypeName());
            configPrice.put(SaleModelConfigType.MODEL.name(), mc.getTypePrice());
            totalPrice = totalPrice.add(mc.getTypePrice());
        }

        SaleModelConfigResult stc = configMap.get(SaleModelConfigType.SPARE_TIRE.name() + "_" + spareTireCode);
        if (stc != null) {
            if (StrUtil.isNotBlank(stc.getTypeName())) desc.append(stc.getTypeName());
            configName.put(SaleModelConfigType.SPARE_TIRE.name(), stc.getTypeName());
            configPrice.put(SaleModelConfigType.SPARE_TIRE.name(), stc.getTypePrice());
            totalPrice = totalPrice.add(stc.getTypePrice());
        }

        SaleModelConfigResult ec = configMap.get(SaleModelConfigType.EXTERIOR.name() + "_" + exteriorCode);
        if (ec != null) {
            if (StrUtil.isNotBlank(ec.getTypeName())) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append(ec.getTypeName());
            }
            configName.put(SaleModelConfigType.EXTERIOR.name(), ec.getTypeName());
            configPrice.put(SaleModelConfigType.EXTERIOR.name(), ec.getTypePrice());
            totalPrice = totalPrice.add(ec.getTypePrice());
            if (ec.getTypeImage() != null && !ec.getTypeImage().isEmpty()) {
                images.add(ec.getTypeImage().get(0));
            }
        }

        SaleModelConfigResult wc = configMap.get(SaleModelConfigType.WHEEL.name() + "_" + wheelCode);
        if (wc != null) {
            if (StrUtil.isNotBlank(wc.getTypeName())) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append(wc.getTypeName());
            }
            configName.put(SaleModelConfigType.WHEEL.name(), wc.getTypeName());
            configPrice.put(SaleModelConfigType.WHEEL.name(), wc.getTypePrice());
            totalPrice = totalPrice.add(wc.getTypePrice());
        }

        SaleModelConfigResult ic = configMap.get(SaleModelConfigType.INTERIOR.name() + "_" + interiorCode);
        if (ic != null) {
            if (StrUtil.isNotBlank(ic.getTypeName())) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append(ic.getTypeName());
            }
            configName.put(SaleModelConfigType.INTERIOR.name(), ic.getTypeName());
            configPrice.put(SaleModelConfigType.INTERIOR.name(), ic.getTypePrice());
            totalPrice = totalPrice.add(ic.getTypePrice());
            if (ic.getTypeImage() != null && !ic.getTypeImage().isEmpty()) {
                images.add(ic.getTypeImage().get(0));
            }
        }

        SaleModelConfigResult ac = configMap.get(SaleModelConfigType.ADAS.name() + "_" + adasCode);
        if (ac != null) {
            if (StrUtil.isNotBlank(ac.getTypeName())) {
                if (desc.length() > 0) desc.append(" | ");
                desc.append(ac.getTypeName());
            }
            configName.put(SaleModelConfigType.ADAS.name(), ac.getTypeName());
            configPrice.put(SaleModelConfigType.ADAS.name(), ac.getTypePrice());
            totalPrice = totalPrice.add(ac.getTypePrice());
        }

        String benefitsIntro = "";
        PurchaseBenefits benefits = getPurchaseBenefits(saleCode);
        if (benefits != null) benefitsIntro = benefits.getIntro();

        return SelectedSaleModelResult.builder()
                .saleCode(saleCode)
                .earnestMoney(model.getEarnestMoney())
                .earnestMoneyPrice(model.getEarnestMoneyPrice())
                .downPayment(model.getDownPayment())
                .downPaymentPrice(model.getDownPaymentPrice())
                .buildConfigCode(getBuildConfigCode(configType))
                .saleModelImages(images)
                .modelName(modelName)
                .saleModelDesc(desc.toString())
                .saleModelConfigType(configType)
                .saleModelConfigName(configName)
                .saleModelConfigPrice(configPrice)
                .totalPrice(totalPrice)
                .purchaseBenefitsIntro(benefitsIntro)
                .build();
    }

    public PurchaseBenefits getPurchaseBenefits(String saleCode) {
        PurchaseBenefitsPo po = purchaseBenefitsMapper.selectCurrentPoBySaleCode(saleCode);
        return po == null ? null : PurchaseBenefits.builder().intro(po.getIntro()).build();
    }

    public PurchaseAgreement getPurchaseAgreement(String saleCode, Integer type) {
        List<PurchaseAgreementPo> list = purchaseAgreementMapper.selectPoByExample(
                PurchaseAgreementPo.builder().saleCode(saleCode).type(type).build());
        return list.isEmpty() ? null : PurchaseAgreement.builder().detail(list.get(0).getDetail()).build();
    }

    public String getBuildConfigCode(Map<String, String> configType) {
        String modelAndSeat = configType.get(SaleModelConfigType.MODEL.name());
        String[] split = modelAndSeat.split("-");
        String modelCode = split[0];
        String seatCode = split.length > 1 ? split[1] : "OT01";

        String wheelAndTire = configType.get(SaleModelConfigType.WHEEL.name());
        String[] split2 = wheelAndTire.split("-");
        String wheelCode = split2[0];
        String tireCode = split2.length > 1 ? split2[1] : "FB01";

        String code = vmdVehicleModelConfigService.getVehicleBuildConfigCode(
                modelCode,
                configType.get(SaleModelConfigType.EXTERIOR.name()),
                configType.get(SaleModelConfigType.INTERIOR.name()),
                wheelCode,
                tireCode,
                configType.get(SaleModelConfigType.SPARE_TIRE.name()),
                configType.get(SaleModelConfigType.ADAS.name()),
                seatCode);

        if (code == null) {
            throw new BuildConfigCodeNotExistException(modelCode,
                    configType.get(SaleModelConfigType.EXTERIOR.name()),
                    configType.get(SaleModelConfigType.INTERIOR.name()),
                    wheelCode, tireCode,
                    configType.get(SaleModelConfigType.SPARE_TIRE.name()),
                    configType.get(SaleModelConfigType.ADAS.name()),
                    seatCode);
        }
        return code;
    }

    public List<LicenseArea> getLicenseAreaList() {
        List<LicenseArea> list = new ArrayList<>();
        exDictionaryService.getDictionaryMap("province").forEach(p ->
            list.add(LicenseArea.builder().provinceCode(p.get("code").toString()).displayName(p.get("name").toString()).build()));
        exDictionaryService.getDictionaryMap("city").forEach(c ->
            list.add(LicenseArea.builder()
                    .provinceCode(c.get("province_code").toString())
                    .cityCode(c.get("code").toString())
                    .displayName(c.get("name").toString()).build()));
        return list;
    }

public List<SaleModelBuildConfigVo> getBuildConfigList(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        List<SaleModelBuildConfigPo> poList = saleModelBuildConfigRepository.findBySaleCode(model.getSaleCode());
        return poList.stream().map(po -> {
            SaleModelBuildConfigVo vo = new SaleModelBuildConfigVo();
            vo.setId(po.getId());
            vo.setSaleCode(po.getSaleCode());
            vo.setBuildConfigCode(po.getBuildConfigCode());
            vo.setEnable(po.getEnable());
            vo.setSort(po.getSort());

            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(po.getBuildConfigCode());
            if (buildConfig != null) {
                vo.setBuildConfigName(buildConfig.getName());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    public List<FeatureCodeRangeVo> getAggregatedFeatureCodeRanges(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        List<SaleModelBuildConfigPo> poList = saleModelBuildConfigRepository.findBySaleCode(model.getSaleCode());

        Map<String, FeatureCodeRangeVo> aggregatedRanges = new LinkedHashMap<>();

        for (SaleModelBuildConfigPo po : poList) {
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(po.getBuildConfigCode());
            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                for (VmdBuildConfigFeatureCodeResponse fc : buildConfig.getFeatureCodes()) {
                    String familyCode = fc.getFamilyCode();

                    FeatureCodeRangeVo range = aggregatedRanges.computeIfAbsent(familyCode, k -> {
                        FeatureCodeRangeVo newRange = new FeatureCodeRangeVo();
                        newRange.setFamilyCode(familyCode);
                        newRange.setFamilyName(fc.getFamilyName());
                        newRange.setFeatureCode(new String[]{});
                        newRange.setFeatureName(new String[]{});
                        return newRange;
                    });

                    if (fc.getFeatureCode() != null && fc.getFeatureCode().length > 0) {
                        List<String> existingCodes = new ArrayList<>(Arrays.asList(range.getFeatureCode()));
                        List<String> existingNames = new ArrayList<>(Arrays.asList(range.getFeatureName()));

                        for (int i = 0; i < fc.getFeatureCode().length; i++) {
                            String code = fc.getFeatureCode()[i];
                            if (!existingCodes.contains(code)) {
                                existingCodes.add(code);
                                existingNames.add(fc.getFeatureName()[i]);
                            }
                        }

                        range.setFeatureCode(existingCodes.toArray(new String[0]));
                        range.setFeatureName(existingNames.toArray(new String[0]));
                    }
                }
            }
        }

        return new ArrayList<>(aggregatedRanges.values());
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createBuildConfig(Long saleModelId, SaleModelBuildConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        if (saleModelBuildConfigRepository.findBySaleCodeAndBuildConfigCode(model.getSaleCode(), dto.getBuildConfigCode()).isPresent()) {
            throw new IllegalArgumentException("该生产配置已关联：" + dto.getBuildConfigCode());
        }
        SaleModelBuildConfigPo po = SaleModelBuildConfigPo.builder()
                .saleCode(model.getSaleCode())
                .buildConfigCode(dto.getBuildConfigCode())
                .enable(dto.getEnable() != null ? dto.getEnable() : true)
                .sort(dto.getSort() != null ? dto.getSort() : 0)
                .rowValid(true)
                .rowVersion(0)
                .createBy(userId)
                .modifyBy(userId)
                .build();
        saleModelBuildConfigRepository.insert(po);
        
        // 自动生成或更新SaleModelConfig
        syncSaleModelConfigFromBuildConfigs(model.getSaleCode(), userId);
        
        return po.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBuildConfig(Long saleModelId, SaleModelBuildConfigDto dto, String userId) {
        SaleModelBuildConfigPo po = saleModelBuildConfigRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("关联配置不存在"));
        po.setEnable(dto.getEnable());
        po.setSort(dto.getSort());
        po.setModifyBy(userId);
        saleModelBuildConfigRepository.update(po);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBuildConfig(Long saleModelId, Long[] ids) {
        SaleModelResult model = getSaleModelById(saleModelId);
        saleModelBuildConfigRepository.physicalDeleteByIds(ids);
        
        // 重新同步SaleModelConfig
        syncSaleModelConfigFromBuildConfigs(model.getSaleCode(), SecurityContextHolder.getUserId());
    }
    
    @Transactional(rollbackFor = Exception.class)
    public void syncSaleModelConfigFromBuildConfigs(String saleCode, String userId) {
        log.info("开始同步销售车型配置，saleCode: {}", saleCode);
        List<SaleModelBuildConfigPo> buildConfigs = saleModelBuildConfigRepository.findBySaleCode(saleCode);
        log.info("找到 {} 个生产配置关联", buildConfigs.size());
        
        // 获取现有的配置
        List<SaleModelConfigPo> existingConfigs = saleModelConfigRepository.findBySaleCode(saleCode);
        Map<String, SaleModelConfigPo> existingMap = existingConfigs.stream()
                .collect(Collectors.toMap(c -> c.getType() + "_" + c.getTypeCode(), c -> c));
        
        // 聚合所有特征值
        Map<String, Set<String>> featureValueMap = new LinkedHashMap<>();
        Map<String, String> featureNameMap = new HashMap<>();
        Map<String, String> featureValueNameMap = new HashMap<>();  // 特征值代码到名称的映射
        
        for (SaleModelBuildConfigPo bc : buildConfigs) {
            log.info("处理生产配置关联: buildConfigCode={}, enable={}", bc.getBuildConfigCode(), bc.getEnable());
            if (!bc.getEnable()) continue;
            
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(bc.getBuildConfigCode());
            log.info("VMD返回数据: buildConfig={}, featureCodes={}", 
                    buildConfig != null ? "存在" : "null",
                    buildConfig != null && buildConfig.getFeatureCodes() != null ? buildConfig.getFeatureCodes().size() : "null");
            
            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                for (VmdBuildConfigFeatureCodeResponse fc : buildConfig.getFeatureCodes()) {
                    String familyCode = fc.getFamilyCode();
                    String familyName = fc.getFamilyName();
                    log.info("特征族: familyCode={}, familyName={}", familyCode, familyName);
                    
                    featureValueMap.computeIfAbsent(familyCode, k -> new LinkedHashSet<>());
                    featureNameMap.put(familyCode, familyName);
                    
                    if (fc.getFeatureCode() != null && fc.getFeatureName() != null) {
                        for (int i = 0; i < fc.getFeatureCode().length; i++) {
                            String code = fc.getFeatureCode()[i];
                            String name = i < fc.getFeatureName().length ? fc.getFeatureName()[i] : null;
                            
                            featureValueMap.get(familyCode).add(code);
                            // 存储特征值名称，如果没有名称则使用代码
                            featureValueNameMap.put(familyCode + "_" + code, name != null && !name.isEmpty() ? name : code);
                            log.info("添加特征值: familyCode={}, featureCode={}, featureName={}", familyCode, code, name);
                        }
                    } else if (fc.getFeatureCode() != null) {
                        for (String code : fc.getFeatureCode()) {
                            featureValueMap.get(familyCode).add(code);
                            featureValueNameMap.put(familyCode + "_" + code, code);
                            log.info("添加特征值(无名称): familyCode={}, featureCode={}", familyCode, code);
                        }
                    }
                }
            }
        }
        
        log.info("聚合后的特征族数量: {}, 特征值详情: {}", featureValueMap.size(), featureValueMap);
        
        Set<String> toDelete = new HashSet<>(existingMap.keySet());
        
        for (Map.Entry<String, Set<String>> entry : featureValueMap.entrySet()) {
            String familyCode = entry.getKey();
            String familyName = featureNameMap.get(familyCode);
            Set<String> featureCodes = entry.getValue();
            
            log.info("处理特征族: familyCode={}, familyName={}, 特征值数量={}", familyCode, familyName, featureCodes.size());
            
            for (String featureCode : featureCodes) {
                String key = familyCode + "_" + featureCode;
                toDelete.remove(key);
                
                // 获取特征值名称，如果没有则使用代码
                String featureValueName = featureValueNameMap.getOrDefault(key, featureCode);
                String typeName = familyName + "-" + featureValueName;
                
                SaleModelConfigPo existing = existingMap.get(key);
                if (existing == null) {
                    SaleModelConfigPo newConfig = SaleModelConfigPo.builder()
                            .saleCode(saleCode)
                            .type(familyCode)
                            .typeCode(featureCode)
                            .typeName(typeName)
                            .typePrice(BigDecimal.ZERO)
                            .typeImage(null)
                            .typeDesc("")
                            .typeParam("")
                            .enable(true)
                            .sort(0)
                            .rowValid(true)
                            .rowVersion(0)
                            .createBy(userId)
                            .modifyBy(userId)
                            .build();
                    saleModelConfigRepository.insert(newConfig);
                    log.info("新增配置项: saleCode={}, type={}, typeCode={}, typeName={}", saleCode, familyCode, featureCode, typeName);
                } else {
                    // 如果已存在但被禁用，重新启用并更新名称
                    if (!existing.getEnable()) {
                        existing.setEnable(true);
                        existing.setTypeName(typeName);
                        existing.setModifyBy(userId);
                        saleModelConfigRepository.update(existing);
                        log.info("重新启用配置项: saleCode={}, type={}, typeCode={}, typeName={}", saleCode, familyCode, featureCode, typeName);
                    } else {
                        log.info("配置项已存在且启用: saleCode={}, type={}, typeCode={}", saleCode, familyCode, featureCode);
                    }
                }
            }
        }
        
        log.info("待清理的配置项数量: {}", toDelete.size());
        for (String key : toDelete) {
            SaleModelConfigPo config = existingMap.get(key);
            if (config != null) {
                // 物理删除不再可选的配置项（历史遗留数据）
                saleModelConfigRepository.physicalDeleteBySaleCodeAndIds(saleCode, new Long[]{config.getId()});
                log.info("物理删除配置项: saleCode={}, type={}, typeCode={}", saleCode, config.getType(), config.getTypeCode());
            }
        }
        
        log.info("同步完成，新增 {} 个，删除 {} 个", featureValueMap.values().stream().mapToInt(Set::size).sum(), toDelete.size());
    }
}
