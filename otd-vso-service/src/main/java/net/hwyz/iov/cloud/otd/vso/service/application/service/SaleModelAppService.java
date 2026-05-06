package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigFeatureCodeResponse;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.framework.common.enums.Symbol;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelResultAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBuildConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseAgreementMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseBenefitsMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.*;
import net.hwyz.iov.cloud.tsp.dictionary.api.feign.service.ExDictionaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    /**
     * 获取销售车型配置的两层结构列表（特征族+特征值）
     */
    public List<SaleModelConfigFamilyVo> getSaleModelConfigFamilyList(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        return getSaleModelConfigFamilyList(model.getSaleCode());
    }

    /**
     * 获取销售车型配置的两层结构列表（特征族+特征值）
     */
    public List<SaleModelConfigFamilyVo> getSaleModelConfigFamilyList(String saleCode) {
        List<SaleModelConfigPo> allConfigs = saleModelConfigRepository.findBySaleCode(saleCode);

        Map<String, SaleModelConfigPo> familyMap = allConfigs.stream()
                .filter(c -> StrUtil.isBlank(c.getTypeCode()))
                .collect(Collectors.toMap(SaleModelConfigPo::getType, c -> c, (a, b) -> a, LinkedHashMap::new));

        Map<String, List<SaleModelConfigPo>> featureMap = allConfigs.stream()
                .filter(c -> StrUtil.isNotBlank(c.getTypeCode()))
                .collect(Collectors.groupingBy(SaleModelConfigPo::getType, LinkedHashMap::new, Collectors.toList()));

        List<SaleModelConfigFamilyVo> result = new ArrayList<>();
        for (Map.Entry<String, SaleModelConfigPo> familyEntry : familyMap.entrySet()) {
            String familyCode = familyEntry.getKey();
            SaleModelConfigPo familyPo = familyEntry.getValue();

            SaleModelConfigFamilyVo familyVo = SaleModelConfigFamilyVo.builder()
                    .familyId(familyPo.getId())
                    .familyCode(familyCode)
                    .familyName(familyPo.getTypeName())
                    .familyPrice(familyPo.getTypePrice())
                    .familyImage(parseJsonToList(familyPo.getTypeImage()))
                    .familyDesc(familyPo.getTypeDesc())
                    .familyParam(familyPo.getTypeParam())
                    .enable(familyPo.getEnable())
                    .sort(familyPo.getSort())
                    .build();

            List<SaleModelConfigVo> featureVoList = new ArrayList<>();
            List<SaleModelConfigPo> features = featureMap.get(familyCode);
            if (features != null) {
                for (SaleModelConfigPo featurePo : features) {
                    SaleModelConfigVo featureVo = SaleModelConfigVo.builder()
                            .id(featurePo.getId())
                            .saleCode(featurePo.getSaleCode())
                            .type(featurePo.getType())
                            .typeCode(featurePo.getTypeCode())
                            .typeName(featurePo.getTypeName())
                            .typePrice(featurePo.getTypePrice())
                            .typeImage(parseJsonToList(featurePo.getTypeImage()))
                            .enable(featurePo.getEnable())
                            .sort(featurePo.getSort())
                            .createTime(featurePo.getCreateTime() != null ? featurePo.getCreateTime().toInstant() : null)
                            .createBy(featurePo.getCreateBy())
                            .modifyTime(featurePo.getModifyTime() != null ? featurePo.getModifyTime().toInstant() : null)
                            .modifyBy(featurePo.getModifyBy())
                            .build();
                    featureVoList.add(featureVo);
                }
            }
            familyVo.setFeatures(featureVoList);
            result.add(familyVo);
        }

        return result;
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return cn.hutool.json.JSONUtil.toList(json, String.class);
        } catch (Exception e) {
            log.warn("解析JSON图片列表失败: {}", json, e);
            return new ArrayList<>();
        }
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

    public PurchaseBenefits getPurchaseBenefits(String saleCode) {
        PurchaseBenefitsPo po = purchaseBenefitsMapper.selectCurrentPoBySaleCode(saleCode);
        return po == null ? null : PurchaseBenefits.builder().intro(po.getIntro()).build();
    }

    public PurchaseAgreement getPurchaseAgreement(String saleCode, Integer type) {
        List<PurchaseAgreementPo> list = purchaseAgreementMapper.selectPoByExample(
                PurchaseAgreementPo.builder().saleCode(saleCode).type(type).build());
        return list.isEmpty() ? null : PurchaseAgreement.builder().detail(list.get(0).getDetail()).build();
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

    /**
     * 根据动态特征值获取已选择的销售车型（新方法）
     *
     * @param saleCode     销售代码
     * @param featureCodes 特征值选择 Map<特征族编码, 特征值编码>
     * @return 已选择的销售车型信息
     */
    public SelectedSaleModelResult getSelectedSaleModelByFeatureCodes(String saleCode, Map<String, String> featureCodes) {
        List<SaleModelPo> list = saleModelRepository.findByCondition(SaleModelQuery.builder()
                .saleCode(saleCode)
                .build());
        if (list.isEmpty()) throw new SaleModelNotExistException(saleCode);
        SaleModelPo model = list.get(0);

        Map<String, SaleModelConfigResult> configMap = getSaleModelConfigMap(saleCode);

        List<String> images = new ArrayList<>();
        StringBuilder desc = new StringBuilder();
        Map<String, String> configName = new LinkedHashMap<>();
        Map<String, BigDecimal> configPrice = new LinkedHashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Map.Entry<String, String> entry : featureCodes.entrySet()) {
            String familyCode = entry.getKey();
            String featureCode = entry.getValue();

            String key = familyCode + "_" + featureCode;
            SaleModelConfigResult config = configMap.get(key);

            if (config != null) {
                configName.put(familyCode, config.getTypeName());
                configPrice.put(familyCode, config.getTypePrice());
                totalPrice = totalPrice.add(config.getTypePrice());

                if (StrUtil.isNotBlank(config.getTypeName())) {
                    if (desc.length() > 0) desc.append(" | ");
                    desc.append(config.getTypeName());
                }

                if (config.getTypeImage() != null && !config.getTypeImage().isEmpty()) {
                    images.add(config.getTypeImage().get(0));
                }
            }
        }

        String buildConfigCode = matchBuildConfigCode(saleCode, featureCodes);

        String benefitsIntro = "";
        PurchaseBenefits benefits = getPurchaseBenefits(saleCode);
        if (benefits != null) benefitsIntro = benefits.getIntro();

        String modelName = "";
        for (Map.Entry<String, String> entry : featureCodes.entrySet()) {
            SaleModelConfigResult config = configMap.get(entry.getKey() + "_" + entry.getValue());
            if (config != null && config.getTypeName() != null) {
                modelName = config.getTypeName();
                break;
            }
        }

        return SelectedSaleModelResult.builder()
                .saleCode(saleCode)
                .modelName(modelName)
                .earnestMoney(model.getEarnestMoney())
                .earnestMoneyPrice(model.getEarnestMoneyPrice())
                .downPayment(model.getDownPayment())
                .downPaymentPrice(model.getDownPaymentPrice())
                .buildConfigCode(buildConfigCode)
                .saleModelImages(images)
                .saleModelDesc(desc.toString())
                .saleModelConfigType(featureCodes)
                .saleModelConfigName(configName)
                .saleModelConfigPrice(configPrice)
                .totalPrice(totalPrice)
                .purchaseBenefitsIntro(benefitsIntro)
                .build();
    }

    /**
     * 根据选择的特征值匹配 BuildConfigCode
     *
     * @param saleCode     销售代码
     * @param featureCodes 特征值选择
     * @return 匹配的生产配置代码，如果没有匹配到返回null
     */
    private String matchBuildConfigCode(String saleCode, Map<String, String> featureCodes) {
        List<SaleModelBuildConfigPo> buildConfigs = saleModelBuildConfigRepository.findBySaleCode(saleCode);

        for (SaleModelBuildConfigPo bc : buildConfigs) {
            if (!bc.getEnable()) continue;

            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(bc.getBuildConfigCode());

            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                boolean matched = checkFeatureCodesMatch(buildConfig.getFeatureCodes(), featureCodes);
                if (matched) {
                    return bc.getBuildConfigCode();
                }
            }
        }

        return null;
    }

    /**
     * 检查特征值是否匹配
     *
     * @param featureCodesFromBuildConfig 生产配置的特征值列表
     * @param selectedFeatureCodes        用户选择的特征值
     * @return 是否匹配
     */
    private boolean checkFeatureCodesMatch(List<VmdBuildConfigFeatureCodeResponse> featureCodesFromBuildConfig,
                                           Map<String, String> selectedFeatureCodes) {
        if (featureCodesFromBuildConfig == null || selectedFeatureCodes == null) {
            return false;
        }

        Map<String, Set<String>> buildConfigFeatureMap = new HashMap<>();
        for (VmdBuildConfigFeatureCodeResponse fc : featureCodesFromBuildConfig) {
            String familyCode = fc.getFamilyCode();
            buildConfigFeatureMap.computeIfAbsent(familyCode, k -> new HashSet<>());
            if (fc.getFeatureCode() != null) {
                for (String code : fc.getFeatureCode()) {
                    buildConfigFeatureMap.get(familyCode).add(code);
                }
            }
        }

        for (Map.Entry<String, String> entry : selectedFeatureCodes.entrySet()) {
            String familyCode = entry.getKey();
            String featureCode = entry.getValue();

            Set<String> allowedCodes = buildConfigFeatureMap.get(familyCode);
            if (allowedCodes == null || !allowedCodes.contains(featureCode)) {
                return false;
            }
        }

        return true;
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
        String saleCode = model.getSaleCode();

        List<SaleModelBuildConfigPo> poList = saleModelBuildConfigRepository.findBySaleCode(saleCode);
        List<SaleModelConfigPo> configPoList = saleModelConfigRepository.findBySaleCode(saleCode);

        Map<String, SaleModelConfigPo> configMap = configPoList.stream()
                .collect(Collectors.toMap(c -> c.getType() + "_" + c.getTypeCode(), c -> c));

        Map<String, FeatureCodeRangeVo> aggregatedRanges = new LinkedHashMap<>();

        for (SaleModelBuildConfigPo po : poList) {
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(po.getBuildConfigCode());
            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                for (VmdBuildConfigFeatureCodeResponse fc : buildConfig.getFeatureCodes()) {
                    String familyCode = fc.getFamilyCode();
                    String familyName = fc.getFamilyName();

                    FeatureCodeRangeVo range = aggregatedRanges.computeIfAbsent(familyCode, k -> {
                        FeatureCodeRangeVo newRange = new FeatureCodeRangeVo();
                        newRange.setFamilyCode(familyCode);
                        newRange.setFamilyName(familyName);
                        newRange.setFeatureDetails(new ArrayList<>());
                        return newRange;
                    });

                    if (fc.getFeatureCode() != null && fc.getFeatureCode().length > 0) {
                        List<FeatureCodeDetailVo> existingDetails = range.getFeatureDetails();

                        for (int i = 0; i < fc.getFeatureCode().length; i++) {
                            String code = fc.getFeatureCode()[i];

                            if (!existingDetails.stream().anyMatch(d -> d.getFeatureCode().equals(code))) {
                                String key = familyCode + "_" + code;
                                SaleModelConfigPo configPo = configMap.get(key);

                                FeatureCodeDetailVo detailVo = FeatureCodeDetailVo.builder()
                                        .featureCode(code)
                                        .featureName(configPo != null ? configPo.getTypeName() :
                                                (i < fc.getFeatureName().length ? fc.getFeatureName()[i] : code))
                                        .featurePrice(configPo != null ? configPo.getTypePrice() : BigDecimal.ZERO)
                                        .featureImage(configPo != null && configPo.getTypeImage() != null ?
                                                cn.hutool.json.JSONUtil.toList(configPo.getTypeImage(), String.class) :
                                                new ArrayList<>())
                                        .featureDesc(configPo != null ? configPo.getTypeDesc() : "")
                                        .featureParam(configPo != null ? configPo.getTypeParam() : "")
                                        .enable(configPo != null ? configPo.getEnable() : true)
                                        .sort(configPo != null ? configPo.getSort() : 0)
                                        .build();

                                existingDetails.add(detailVo);
                            }
                        }
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
        saleModelBuildConfigRepository.physicalDeleteBySaleCodeAndBuildConfigCode(model.getSaleCode(), dto.getBuildConfigCode());
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

        List<SaleModelConfigPo> existingConfigs = saleModelConfigRepository.findBySaleCode(saleCode);
        Map<String, SaleModelConfigPo> existingMap = existingConfigs.stream()
                .collect(Collectors.toMap(c -> c.getType() + "_" + (c.getTypeCode() == null ? "" : c.getTypeCode()), c -> c));

        Map<String, Set<String>> featureValueMap = new LinkedHashMap<>();
        Map<String, String> featureNameMap = new HashMap<>();
        Map<String, String> featureValueNameMap = new HashMap<>();

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

            String familyKey = familyCode + "_";
            toDelete.remove(familyKey);

            SaleModelConfigPo existingFamily = existingMap.get(familyKey);
            if (existingFamily == null) {
                SaleModelConfigPo newFamily = SaleModelConfigPo.builder()
                        .saleCode(saleCode)
                        .type(familyCode)
                        .typeCode("")
                        .typeName(familyName)
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
                saleModelConfigRepository.insert(newFamily);
                log.info("新增特征族: saleCode={}, type={}, typeName={}", saleCode, familyCode, familyName);
            } else {
                if (!existingFamily.getEnable()) {
                    existingFamily.setEnable(true);
                    existingFamily.setTypeName(familyName);
                    existingFamily.setModifyBy(userId);
                    saleModelConfigRepository.update(existingFamily);
                    log.info("重新启用特征族: saleCode={}, type={}, typeName={}", saleCode, familyCode, familyName);
                } else {
                    log.info("特征族已存在且启用: saleCode={}, type={}", saleCode, familyCode);
                }
            }

            for (String featureCode : featureCodes) {
                String featureKey = familyCode + "_" + featureCode;
                toDelete.remove(featureKey);

                String featureValueName = featureValueNameMap.getOrDefault(featureKey, featureCode);
                String typeName = familyName + "-" + featureValueName;

                SaleModelConfigPo existing = existingMap.get(featureKey);
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
                    log.info("新增特征值: saleCode={}, type={}, typeCode={}, typeName={}", saleCode, familyCode, featureCode, typeName);
                } else {
                    if (!existing.getEnable()) {
                        existing.setEnable(true);
                        existing.setTypeName(typeName);
                        existing.setModifyBy(userId);
                        saleModelConfigRepository.update(existing);
                        log.info("重新启用特征值: saleCode={}, type={}, typeCode={}, typeName={}", saleCode, familyCode, featureCode, typeName);
                    } else {
                        log.info("特征值已存在且启用: saleCode={}, type={}, typeCode={}", saleCode, familyCode, featureCode);
                    }
                }
            }
        }

        log.info("待清理的配置项数量: {}", toDelete.size());
        for (String key : toDelete) {
            SaleModelConfigPo config = existingMap.get(key);
            if (config != null) {
                saleModelConfigRepository.physicalDeleteBySaleCodeAndIds(saleCode, new Long[]{config.getId()});
                String itemType = config.getTypeCode() == null ? "特征族" : "特征值";
                log.info("物理删除{}: saleCode={}, type={}, typeCode={}", itemType, saleCode, config.getType(), config.getTypeCode());
            }
        }

        log.info("同步完成，特征族 {} 个，特征值 {} 个，删除 {} 个", featureValueMap.size(),
                featureValueMap.values().stream().mapToInt(Set::size).sum(), toDelete.size());
    }
}
