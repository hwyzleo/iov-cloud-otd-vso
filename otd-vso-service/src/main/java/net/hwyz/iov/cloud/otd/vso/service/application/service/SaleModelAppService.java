package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.dictionary.api.service.DictionaryService;
import net.hwyz.iov.cloud.edd.dictionary.api.vo.response.DictionaryResponse;
import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.service.OptionCodeService;
import net.hwyz.iov.cloud.edd.mdm.api.service.OptionFamilyService;
import net.hwyz.iov.cloud.edd.mdm.api.service.VariantService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationPageResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.ConfigurationResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.OptionCodePageResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.OptionCodeResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.OptionFamilyPageResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.OptionFamilyResponse;
import net.hwyz.iov.cloud.edd.mdm.api.vo.response.VariantResponse;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigFeatureCodeResponse;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.common.enums.Symbol;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelPoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.assembler.SaleModelResultAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateConfigPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionFamilyPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetConfiguratorCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetQuoteCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ConfiguratorResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.QuoteResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelVariantLockedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.ConfiguratorService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBaseModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelBuildConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelConfigRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionFamilyPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.PurchaseBenefitsMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.*;
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
    private final SaleModelBaseModelRepository saleModelBaseModelRepository;
    private final DictionaryService dictionaryService;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;
    private final PurchaseBenefitsMapper purchaseBenefitsMapper;
    private final ConfiguratorService configuratorService;
    private final SalesPolicyService salesPolicyService;
    private final SaleModelConfigPolicyRepository configPolicyRepository;
    private final SaleModelOptionPolicyRepository optionPolicyRepository;
    private final SaleModelOptionFamilyPolicyRepository optionFamilyPolicyRepository;
    private final OrderRepository orderRepository;
    private final WishlistRepository wishlistRepository;
    private final MdmProjectionService mdmProjectionService;
    private final VariantService variantService;
    private final ConfigurationService configurationService;
    private final OptionCodeService optionCodeService;
    private final OptionFamilyService optionFamilyService;

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

    public SaleModelResult getSaleModelByCode(String saleModelCode) {
        return saleModelRepository.findBySaleModelCode(saleModelCode)
                .map(SaleModelResultAssembler.INSTANCE::toResult)
                .orElseThrow(() -> new SaleModelNotExistException(saleModelCode));
    }

    public boolean checkSaleModelCodeUnique(Long saleModelId, String saleModelCode) {
        return !saleModelRepository.existsBySaleModelCodeExcludeId(saleModelCode, saleModelId);
    }

    public List<SaleModelConfigResult> getSaleModelConfigList(Long saleModelId) {
        return SaleModelResultAssembler.INSTANCE.toConfigResultList(saleModelConfigRepository.findBySaleModelId(saleModelId));
    }

    public List<SaleModelConfigResult> getSaleModelConfigList(String saleModelCode) {
        return SaleModelResultAssembler.INSTANCE.toConfigResultList(saleModelConfigRepository.findBySaleModelCode(saleModelCode));
    }

    /**
     * 获取销售车型配置的两层结构列表（特征族+特征值）
     */
    public List<SaleModelConfigFamilyVo> getSaleModelConfigFamilyList(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        return getSaleModelConfigFamilyList(model.getSaleModelCode());
    }

    /**
     * 获取销售车型配置的两层结构列表（特征族+特征值）
     */
    public List<SaleModelConfigFamilyVo> getSaleModelConfigFamilyList(String saleModelCode) {
        List<SaleModelConfigPo> allConfigs = saleModelConfigRepository.findBySaleModelCode(saleModelCode);

        Map<String, SaleModelConfigPo> familyMap = allConfigs.stream()
                .filter(c -> StrUtil.isBlank(c.getTypeCode()))
                .sorted(Comparator.comparing(c -> c.getSort() != null ? c.getSort() : 0))
                .collect(Collectors.toMap(SaleModelConfigPo::getType, c -> c, (a, b) -> a, LinkedHashMap::new));

        Map<String, List<SaleModelConfigPo>> featureMap = allConfigs.stream()
                .filter(c -> StrUtil.isNotBlank(c.getTypeCode()))
                .sorted(Comparator.comparing(c -> c.getSort() != null ? c.getSort() : 0))
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
                features.sort(Comparator.comparing(c -> c.getSort() != null ? c.getSort() : 0));
                for (SaleModelConfigPo featurePo : features) {
                    SaleModelConfigVo featureVo = SaleModelConfigVo.builder()
                            .id(featurePo.getId())
                            .saleModelCode(featurePo.getSaleModelCode())
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
        return saleModelConfigRepository.findByIdAndSaleModelCode(saleModelConfigId, model.getSaleModelCode())
                .map(SaleModelResultAssembler.INSTANCE::toConfigResult)
                .orElse(null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSaleModel(SaleModelCreateDto dto, String userId) {
        // 校验 saleModelCode 唯一性
        if (!checkSaleModelCodeUnique(null, dto.getSaleModelCode())) {
            throw new IllegalArgumentException("销售代码已存在：" + dto.getSaleModelCode());
        }
        
        // 校验 variantCode 1:1 约束：同 variantCode 已存在 SaleModel 时拒绝创建
        if (dto.getVariantCode() != null && !dto.getVariantCode().isEmpty()) {
            boolean variantExists = saleModelRepository.existsByVariantCode(dto.getVariantCode());
            if (variantExists) {
                throw new IllegalArgumentException("同 Variant 编码已存在销售车型，不允许重复绑定：" + dto.getVariantCode());
            }
        }
        
        SaleModelPo entity = SaleModelPoAssembler.INSTANCE.toDo(dto);
        entity.setCreateBy(userId);
        entity.setModifyBy(userId);
        // 设置默认值
        if (entity.getEarnestMoney() == null) {
            entity.setEarnestMoney(false);
        }
        if (entity.getDownPayment() == null) {
            entity.setDownPayment(false);
        }
        if (entity.getEnable() == null) {
            entity.setEnable(true);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        saleModelRepository.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createSaleModelConfig(Long saleModelId, SaleModelConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        SaleModelConfigPo entity = SaleModelPoAssembler.INSTANCE.toConfigDo(dto);
        entity.setSaleModelCode(model.getSaleModelCode());
        entity.setCreateBy(userId);
        entity.setModifyBy(userId);
        saleModelConfigRepository.insert(entity);
        return entity.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySaleModel(SaleModelUpdateDto dto, String userId) {
        if (!checkSaleModelCodeUnique(dto.getId(), dto.getSaleModelCode())) {
            throw new IllegalArgumentException("销售代码已存在：" + dto.getSaleModelCode());
        }
        
        // 校验 variantCode 修改锁定：如果要修改 variantCode，需要校验"无未完成订单 + 无活跃心愿单"
        if (dto.getVariantCode() != null) {
            SaleModelPo existingModel = saleModelRepository.findById(dto.getId())
                .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在"));
            
            // 如果 variantCode 发生了变化，需要校验锁定
            if (existingModel.getVariantCode() != null && !existingModel.getVariantCode().equals(dto.getVariantCode())) {
                // 校验是否有未完成订单
                boolean hasActiveOrders = orderRepository.existsActiveOrdersBySaleModelCode(dto.getSaleModelCode());
                // 校验是否有活跃心愿单
                boolean hasActiveWishlists = wishlistRepository.existsActiveBySaleModelCode(dto.getSaleModelCode());
                
                if (hasActiveOrders || hasActiveWishlists) {
                    throw new SaleModelVariantLockedException(
                        String.format("销售车型 [%s] 已有活跃订单或心愿单，不可修改 variantCode", dto.getSaleModelCode()));
                }
                
                // 校验新 variantCode 是否已被其他 SaleModel 使用
                boolean newVariantExists = saleModelRepository.existsByVariantCodeExcludeId(dto.getVariantCode(), dto.getId());
                if (newVariantExists) {
                    throw new IllegalArgumentException("同 Variant 编码已存在其他销售车型，不允许重复绑定：" + dto.getVariantCode());
                }
            }
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
        entity.setSaleModelCode(model.getSaleModelCode());
        entity.setModifyBy(userId);
        saleModelConfigRepository.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifySaleModelImages(String saleModelCode, List<String> images, String userId) {
        SaleModelResult result = getSaleModelByCode(saleModelCode);
        SaleModelPo entity = saleModelRepository.findById(result.getId()).orElseThrow();
        entity.setImages(cn.hutool.json.JSONUtil.toJsonStr(images));
        entity.setModifyBy(userId);
        saleModelRepository.update(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleModelByIds(Long[] ids) {
        for (Long id : ids) {
            SaleModelResult model = getSaleModelById(id);
            List<SaleModelConfigPo> configs = saleModelConfigRepository.findBySaleModelCode(model.getSaleModelCode());
            if (!configs.isEmpty()) {
                Long[] configIds = configs.stream().map(SaleModelConfigPo::getId).toArray(Long[]::new);
                saleModelConfigRepository.physicalDeleteBySaleModelCodeAndIds(model.getSaleModelCode(), configIds);
            }
        }
        saleModelRepository.physicalDeleteByIds(ids);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSaleModelConfigByIds(Long saleModelId, Long[] ids) {
        if (ids == null || ids.length == 0) return;
        SaleModelResult model = getSaleModelById(saleModelId);
        saleModelConfigRepository.physicalDeleteBySaleModelCodeAndIds(model.getSaleModelCode(), ids);
    }

    public Map<String, SaleModelConfigResult> getSaleModelConfigMap(String saleModelCode) {
        return getSaleModelConfigList(saleModelCode).stream()
                .collect(Collectors.toMap(
                        k -> k.getType() + Symbol.UNDERSCORE.value + k.getTypeCode(),
                        v -> v,
                        (v1, v2) -> v1));
    }

    public PurchaseBenefits getPurchaseBenefits(String saleModelCode) {
        PurchaseBenefitsPo po = purchaseBenefitsMapper.selectCurrentPoBySaleCode(saleModelCode);
        return po == null ? null : PurchaseBenefits.builder().intro(po.getIntro()).build();
    }

    /**
     * 获取选配器数据
     */
    public ConfiguratorResult getConfigurator(GetConfiguratorCmd cmd) {
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(cmd.getSaleModelCode())
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + cmd.getSaleModelCode()));

        if (!"active".equals(saleModel.getListingStatus())) {
            throw new SaleModelNotExistException("销售车型已下架: " + cmd.getSaleModelCode());
        }

        ConfiguratorService.ConfiguratorData data = configuratorService.getConfigurator(
            saleModel.getVariantCode(), cmd.getSaleModelCode(), cmd.getRegionCode());

        // TODO: 组装 ConfiguratorResult
        return null;
    }

    /**
     * 获取实时报价
     */
    public QuoteResult getQuote(GetQuoteCmd cmd) {
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(cmd.getSaleModelCode())
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + cmd.getSaleModelCode()));

        salesPolicyService.validateOptionsForSale(cmd.getSaleModelCode(), cmd.getOptionCodes(), cmd.getRegionCode());

        String configurationCode = vmdVehicleModelConfigService.getBuildConfigCodeByOptionCodes(cmd.getSaleModelCode(), cmd.getOptionCodes());
        if (configurationCode == null) {
            throw new ConfigurationNotMatchedException("OptionCode 组合无法匹配到合法 Configuration");
        }

        salesPolicyService.validateConfigurationForSale(cmd.getSaleModelCode(), configurationCode);

        BigDecimal basePrice = saleModel.getBasePrice() != null ? saleModel.getBasePrice() : BigDecimal.ZERO;
        BigDecimal totalPrice = configuratorService.calculateTotalPrice(cmd.getSaleModelCode(), basePrice, cmd.getOptionCodes());

        List<QuoteResult.OptionPriceItem> breakdown = cmd.getOptionCodes().stream()
            .map(code -> QuoteResult.OptionPriceItem.builder()
                .optionCode(code)
                .optionPrice(salesPolicyService.getOptionPrice(cmd.getSaleModelCode(), code))
                .build())
            .collect(Collectors.toList());

        return QuoteResult.builder()
            .configurationCode(configurationCode)
            .totalPrice(totalPrice)
            .optionPriceBreakdown(breakdown)
            .build();
    }

    /**
     * 同步 MDM 数据
     * 强制刷新该 variantCode 的本地 MDM 投影
     */
    public Map<String, Integer> syncMdmData(String saleModelCode) {
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode)
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModelCode));

        if (saleModel.getVariantCode() == null || saleModel.getVariantCode().isEmpty()) {
            throw new IllegalArgumentException("销售车型未绑定 Variant，无法同步 MDM 数据");
        }

        String variantCode = saleModel.getVariantCode();
        int variantAdded = 0, variantUpdated = 0;
        int configAdded = 0, configUpdated = 0;
        int optionAdded = 0, optionUpdated = 0, optionDeleted = 0;

        // 获取 Variant 数据
        VariantResponse variantResp = null;
        try {
            variantResp = variantService.getByCode(variantCode);
            if (variantResp != null) {
                MdmProjectionVariantPo existingVariant = mdmProjectionService.getVariantOptional(variantCode).orElse(null);
                MdmProjectionVariantPo variantPo = MdmProjectionVariantPo.builder()
                    .variantCode(variantResp.getCode())
                    .variantName(variantResp.getName())
                    .modelCode(variantResp.getModelCode())
                    .status(variantResp.getStatus())
                    .build();

                if (existingVariant != null) {
                    variantPo.setId(existingVariant.getId());
                    mdmProjectionService.saveOrUpdateVariant(variantPo);
                    variantUpdated++;
                } else {
                    mdmProjectionService.saveOrUpdateVariant(variantPo);
                    variantAdded++;
                }
                log.info("Variant 投影同步完成: {}", variantCode);
            }
        } catch (Exception e) {
            log.error("同步 Variant 投影失败: {}", variantCode, e);
        }

        // 同步 Configuration 投影
        try {
            ConfigurationPageResponse configPageResp = configurationService.listAll(1, 1000, variantCode, true);
            if (configPageResp != null && configPageResp.getRows() != null) {
                for (ConfigurationResponse configResp : configPageResp.getRows()) {
                    MdmProjectionConfigurationPo existingConfig = mdmProjectionService.getConfigurationOptional(configResp.getCode()).orElse(null);
                    MdmProjectionConfigurationPo configPo = MdmProjectionConfigurationPo.builder()
                        .configurationCode(configResp.getCode())
                        .variantCode(configResp.getVariantCode())
                        .status(configResp.getStatus())
                        .build();

                    // 获取 Configuration 关联的 OptionCode 列表
                    try {
                        List<OptionCodeResponse> optionCodeResps = configurationService.getOptionCodes(configResp.getCode());
                        if (optionCodeResps != null && !optionCodeResps.isEmpty()) {
                            List<String> optionCodes = optionCodeResps.stream()
                                .map(OptionCodeResponse::getCode)
                                .collect(Collectors.toList());
                            configPo.setOptionCodes(cn.hutool.json.JSONUtil.toJsonStr(optionCodes));
                        }
                    } catch (Exception e) {
                        log.warn("获取 Configuration [{}] 的 OptionCode 失败: {}", configResp.getCode(), e.getMessage());
                    }

                    if (existingConfig != null) {
                        configPo.setId(existingConfig.getId());
                        mdmProjectionService.saveOrUpdateConfiguration(configPo);
                        configUpdated++;
                    } else {
                        mdmProjectionService.saveOrUpdateConfiguration(configPo);
                        configAdded++;
                    }
                }
                log.info("Configuration 投影同步完成: {}, 数量: {}", variantCode, configPageResp.getRows().size());
            }
        } catch (Exception e) {
            log.error("同步 Configuration 投影失败: {}", variantCode, e);
        }

        // 同步 OptionCode 投影（只同步该 variantCode 关联的 OptionCode）
        // 关联来源：Configuration 的 optionCodes（MDM API 未暴露 Variant 的 standardOptions）
        try {
            Set<String> relatedOptionCodes = new HashSet<>();

            // 从 Configuration 的 optionCodes 中获取
            List<MdmProjectionConfigurationPo> configs = mdmProjectionService.getConfigurationsByVariant(variantCode);
            for (MdmProjectionConfigurationPo config : configs) {
                if (config.getOptionCodes() != null) {
                    relatedOptionCodes.addAll(cn.hutool.json.JSONUtil.toList(config.getOptionCodes(), String.class));
                }
            }

            // 同步关联的 OptionCode
            Set<String> relatedOptionFamilyCodes = new HashSet<>();
            for (String optionCode : relatedOptionCodes) {
                try {
                    OptionCodeResponse optionResp = optionCodeService.getByCode(optionCode);
                    if (optionResp != null) {
                        MdmProjectionOptionPo existingOption = mdmProjectionService.getOptionOptional(optionCode).orElse(null);
                        MdmProjectionOptionPo optionPo = MdmProjectionOptionPo.builder()
                            .optionCode(optionResp.getCode())
                            .optionFamilyCode(optionResp.getOptionFamilyCode())
                            .optionName(optionResp.getName())
                            .status(optionResp.getStatus())
                            .build();

                        if (existingOption != null) {
                            optionPo.setId(existingOption.getId());
                            mdmProjectionService.saveOrUpdateOption(optionPo);
                            optionUpdated++;
                        } else {
                            mdmProjectionService.saveOrUpdateOption(optionPo);
                            optionAdded++;
                        }

                        // 收集关联的 OptionFamily
                        if (optionResp.getOptionFamilyCode() != null) {
                            relatedOptionFamilyCodes.add(optionResp.getOptionFamilyCode());
                        }
                    }
                } catch (Exception e) {
                    log.warn("同步 OptionCode 失败: {}", optionCode, e);
                }
            }
            log.info("OptionCode 投影同步完成: {}, 关联数量: {}", variantCode, relatedOptionCodes.size());

            // 同步关联的 OptionFamily
            for (String optionFamilyCode : relatedOptionFamilyCodes) {
                try {
                    OptionFamilyResponse familyResp = optionFamilyService.getByCode(optionFamilyCode);
                    if (familyResp != null) {
                        MdmProjectionOptionFamilyPo existingFamily = mdmProjectionService.getOptionFamilyOptional(optionFamilyCode).orElse(null);
                        MdmProjectionOptionFamilyPo familyPo = MdmProjectionOptionFamilyPo.builder()
                            .optionFamilyCode(familyResp.getCode())
                            .optionFamilyName(familyResp.getName())
                            .status(familyResp.getStatus())
                            .build();

                        if (existingFamily != null) {
                            familyPo.setId(existingFamily.getId());
                            mdmProjectionService.saveOrUpdateOptionFamily(familyPo);
                        } else {
                            mdmProjectionService.saveOrUpdateOptionFamily(familyPo);
                        }
                    }
                } catch (Exception e) {
                    log.warn("同步 OptionFamily 失败: {}", optionFamilyCode, e);
                }
            }
            log.info("OptionFamily 投影同步完成: {}, 关联数量: {}", variantCode, relatedOptionFamilyCodes.size());
        } catch (Exception e) {
            log.error("同步 OptionCode/OptionFamily 投影失败: {}", variantCode, e);
        }

        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("variantAdded", variantAdded);
        stats.put("variantUpdated", variantUpdated);
        stats.put("configurationAdded", configAdded);
        stats.put("configurationUpdated", configUpdated);
        stats.put("optionAdded", optionAdded);
        stats.put("optionUpdated", optionUpdated);
        stats.put("optionDeleted", optionDeleted);

        log.info("同步 MDM 数据完成，saleModelCode: {}, variantCode: {}, 统计: {}", saleModelCode, variantCode, stats);
        return stats;
    }

    public List<LicenseArea> getLicenseAreaList() {
        List<LicenseArea> list = new ArrayList<>();
        DictionaryResponse province = dictionaryService.getDictionary("province");
        if (province == null || province.getItems() == null) {
            log.warn("获取省份字典失败");
            return list;
        }
        for (Map<String, Object> item : province.getItems()) {
            list.add(LicenseArea.builder()
                    .provinceCode(item.get("code").toString())
                    .displayName(item.get("name").toString())
                    .build());
        }
        DictionaryResponse city = dictionaryService.getDictionary("city");
        if (city == null || city.getItems() == null) {
            log.warn("获取城市字典失败");
            return list;
        }
        for (Map<String, Object> item : city.getItems()) {
            list.add(LicenseArea.builder()
                    .provinceCode(item.get("province_code").toString())
                    .cityCode(item.get("code").toString())
                    .displayName(item.get("name").toString())
                    .build());
        }
        return list;
    }

    /**
     * 根据动态特征值获取已选择的销售车型（新方法）
     *
     * @param saleModelCode     销售代码
     * @param featureCodes 特征值选择 Map<特征族编码, 特征值编码>
     * @return 已选择的销售车型信息
     */
    public SelectedSaleModelResult getSelectedSaleModelByFeatureCodes(String saleModelCode, Map<String, String> featureCodes) {
        List<SaleModelPo> list = saleModelRepository.findByCondition(SaleModelQuery.builder()
                .saleModelCode(saleModelCode)
                .build());
        if (list.isEmpty()) throw new SaleModelNotExistException(saleModelCode);
        SaleModelPo model = list.get(0);

        Map<String, SaleModelConfigResult> configMap = getSaleModelConfigMap(saleModelCode);

        List<String> images = new ArrayList<>();
        StringBuilder desc = new StringBuilder();
        Map<String, String> configName = new LinkedHashMap<>();
        Map<String, BigDecimal> configPrice = new LinkedHashMap<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        String baseModelCode = null;
        String modelName = "";

        // 处理基础车型选择
        if (featureCodes.containsKey("BASE_MODEL")) {
            baseModelCode = featureCodes.get("BASE_MODEL");
            SaleModelBaseModelPo baseModelPo = saleModelBaseModelRepository.findBySaleModelCodeAndBaseModelCode(saleModelCode, baseModelCode)
                    .orElse(null);
            if (baseModelPo != null) {
                modelName = baseModelPo.getBaseModelName();
                configName.put("BASE_MODEL", baseModelPo.getBaseModelName());
                configPrice.put("BASE_MODEL", baseModelPo.getBaseModelPrice() != null ? baseModelPo.getBaseModelPrice() : BigDecimal.ZERO);
                totalPrice = totalPrice.add(baseModelPo.getBaseModelPrice() != null ? baseModelPo.getBaseModelPrice() : BigDecimal.ZERO);

                if (StrUtil.isNotBlank(baseModelPo.getBaseModelName())) {
                    desc.append(baseModelPo.getBaseModelName());
                }

                if (baseModelPo.getBaseModelImage() != null && !baseModelPo.getBaseModelImage().isEmpty()) {
                    List<String> baseModelImages = cn.hutool.json.JSONUtil.toList(baseModelPo.getBaseModelImage(), String.class);
                    if (!baseModelImages.isEmpty()) {
                        images.add(baseModelImages.get(0));
                    }
                }
            }
        }

        // 处理其他特征值选择
        for (Map.Entry<String, String> entry : featureCodes.entrySet()) {
            String familyCode = entry.getKey();
            String featureCode = entry.getValue();

            if (familyCode.equals("BASE_MODEL")) continue;

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

        String buildConfigCode = matchBuildConfigCode(saleModelCode, featureCodes, baseModelCode);

        String benefitsIntro = "";
        PurchaseBenefits benefits = getPurchaseBenefits(saleModelCode);
        if (benefits != null) benefitsIntro = benefits.getIntro();

        if (modelName.isEmpty()) {
            for (Map.Entry<String, String> entry : featureCodes.entrySet()) {
                if (!entry.getKey().equals("BASE_MODEL")) {
                    SaleModelConfigResult config = configMap.get(entry.getKey() + "_" + entry.getValue());
                    if (config != null && config.getTypeName() != null) {
                        modelName = config.getTypeName();
                        break;
                    }
                }
            }
        }

        return SelectedSaleModelResult.builder()
                .saleModelCode(saleModelCode)
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
     * @param saleModelCode      销售代码
     * @param featureCodes  特征值选择
     * @param baseModelCode 基础车型代码（可选）
     * @return 匹配的生产配置代码，如果没有匹配到返回null
     */
    private String matchBuildConfigCode(String saleModelCode, Map<String, String> featureCodes, String baseModelCode) {
        Map<String, String> featureConfig = new HashMap<>(featureCodes);
        featureConfig.remove("BASE_MODEL");

        return vmdVehicleModelConfigService.getVehicleBuildConfigCode(featureConfig);
    }

    public List<SaleModelBuildConfigVo> getBuildConfigPageBySaleCode(String saleModelCode) {
        List<SaleModelBuildConfigPo> poList = saleModelBuildConfigRepository.findBySaleModelCode(saleModelCode);
        return PageUtil.convert(poList, po -> {
            SaleModelBuildConfigVo vo = new SaleModelBuildConfigVo();
            vo.setId(po.getId());
            vo.setSaleModelCode(po.getSaleModelCode());
            vo.setBuildConfigCode(po.getBuildConfigCode());
            vo.setEnable(po.getEnable());
            vo.setSort(po.getSort());

            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(po.getBuildConfigCode());
            if (buildConfig != null) {
                vo.setBuildConfigName(buildConfig.getName());
            }
            return vo;
        });
    }

    public List<FeatureCodeRangeVo> getAggregatedFeatureCodeRanges(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        String saleModelCode = model.getSaleModelCode();

        List<FeatureCodeRangeVo> result = new ArrayList<>();

        // 构建基础车型特征族作为第一个选择项
        List<SaleModelBaseModelPo> baseModelList = saleModelBaseModelRepository.findBySaleModelCode(saleModelCode);
        if (!baseModelList.isEmpty()) {
            FeatureCodeRangeVo baseModelRange = new FeatureCodeRangeVo();
            baseModelRange.setFamilyCode("BASE_MODEL");
            baseModelRange.setFamilyName("车型");
            baseModelRange.setFamilyPrice(BigDecimal.ZERO);
            baseModelRange.setFamilyImage(new ArrayList<>());
            baseModelRange.setFamilyDesc("");
            baseModelRange.setFamilyParam("");
            baseModelRange.setEnable(true);
            baseModelRange.setSort(-1);

            List<FeatureCodeDetailVo> baseModelDetails = new ArrayList<>();
            for (SaleModelBaseModelPo po : baseModelList) {
                if (po.getEnable()) {
                    FeatureCodeDetailVo detailVo = FeatureCodeDetailVo.builder()
                            .featureCode(po.getBaseModelCode())
                            .featureName(po.getBaseModelName())
                            .featurePrice(po.getBaseModelPrice() != null ? po.getBaseModelPrice() : BigDecimal.ZERO)
                            .featureImage(po.getBaseModelImage() != null ?
                                    cn.hutool.json.JSONUtil.toList(po.getBaseModelImage(), String.class) :
                                    new ArrayList<>())
                            .featureDesc(po.getBaseModelDesc() != null ? po.getBaseModelDesc() : "")
                            .featureParam(po.getBaseModelParam() != null ? po.getBaseModelParam() : "")
                            .enable(po.getEnable())
                            .sort(po.getSort() != null ? po.getSort() : 0)
                            .build();
                    baseModelDetails.add(detailVo);
                }
            }
            baseModelDetails.sort(Comparator.comparing(d -> d.getSort() != null ? d.getSort() : 0));
            baseModelRange.setFeatureDetails(baseModelDetails);
            result.add(baseModelRange);
        }

        // 构建其他特征族
        List<SaleModelBuildConfigPo> poList = saleModelBuildConfigRepository.findBySaleModelCode(saleModelCode);
        List<SaleModelConfigPo> configPoList = saleModelConfigRepository.findBySaleModelCode(saleModelCode);

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

                        String familyKey = familyCode + "_";
                        SaleModelConfigPo familyConfig = configMap.get(familyKey);
                        if (familyConfig != null) {
                            newRange.setFamilyName(familyConfig.getTypeName() != null ? familyConfig.getTypeName() : familyName);
                            newRange.setFamilyPrice(familyConfig.getTypePrice());
                            newRange.setFamilyImage(familyConfig.getTypeImage() != null ?
                                    cn.hutool.json.JSONUtil.toList(familyConfig.getTypeImage(), String.class) :
                                    new ArrayList<>());
                            newRange.setFamilyDesc(familyConfig.getTypeDesc());
                            newRange.setFamilyParam(familyConfig.getTypeParam());
                            newRange.setEnable(familyConfig.getEnable());
                            newRange.setSort(familyConfig.getSort());
                        } else {
                            newRange.setFamilyPrice(BigDecimal.ZERO);
                            newRange.setFamilyImage(new ArrayList<>());
                            newRange.setFamilyDesc("");
                            newRange.setFamilyParam("");
                            newRange.setEnable(true);
                            newRange.setSort(0);
                        }

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

        List<FeatureCodeRangeVo> otherRanges = new ArrayList<>(aggregatedRanges.values());
        otherRanges.sort(Comparator.comparing(r -> r.getSort() != null ? r.getSort() : 0));
        for (FeatureCodeRangeVo range : otherRanges) {
            if (range.getFeatureDetails() != null) {
                range.getFeatureDetails().sort(Comparator.comparing(d -> d.getSort() != null ? d.getSort() : 0));
            }
        }
        result.addAll(otherRanges);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchCreateBuildConfig(Long saleModelId, SaleModelBuildConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        List<Long> ids = new ArrayList<>();

        List<String> buildConfigCodes = dto.getBuildConfigCodes();
        if (buildConfigCodes == null || buildConfigCodes.isEmpty()) {
            if (dto.getBuildConfigCode() != null && !dto.getBuildConfigCode().isEmpty()) {
                buildConfigCodes = new ArrayList<>();
                buildConfigCodes.add(dto.getBuildConfigCode());
            } else {
                return ids;
            }
        }

        for (String buildConfigCode : buildConfigCodes) {
            if (saleModelBuildConfigRepository.findBySaleModelCodeAndBuildConfigCode(model.getSaleModelCode(), buildConfigCode).isPresent()) {
                log.info("生产配置已存在关联关系，跳过: saleModelCode={}, buildConfigCode={}", model.getSaleModelCode(), buildConfigCode);
                continue;
            }
            SaleModelBuildConfigPo po = SaleModelBuildConfigPo.builder()
                    .saleModelCode(model.getSaleModelCode())
                    .buildConfigCode(buildConfigCode)
                    .enable(dto.getEnable() != null ? dto.getEnable() : true)
                    .sort(dto.getSort() != null ? dto.getSort() : 0)
                    .rowValid(true)
                    .rowVersion(0)
                    .createBy(userId)
                    .modifyBy(userId)
                    .build();
            saleModelBuildConfigRepository.insert(po);
            ids.add(po.getId());
        }

        if (!ids.isEmpty()) {
            syncSaleModelConfigFromBuildConfigs(model.getSaleModelCode(), userId);
            syncBaseModelFromBuildConfigs(model.getSaleModelCode(), userId);
        }

        return ids;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createBuildConfig(Long saleModelId, SaleModelBuildConfigDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        if (saleModelBuildConfigRepository.findBySaleModelCodeAndBuildConfigCode(model.getSaleModelCode(), dto.getBuildConfigCode()).isPresent()) {
            throw new IllegalArgumentException("该生产配置已关联：" + dto.getBuildConfigCode());
        }
        saleModelBuildConfigRepository.physicalDeleteBySaleModelCodeAndBuildConfigCode(model.getSaleModelCode(), dto.getBuildConfigCode());
        SaleModelBuildConfigPo po = SaleModelBuildConfigPo.builder()
                .saleModelCode(model.getSaleModelCode())
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
        syncSaleModelConfigFromBuildConfigs(model.getSaleModelCode(), userId);

        // 自动生成或更新SaleModelBaseModel
        syncBaseModelFromBuildConfigs(model.getSaleModelCode(), userId);

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
        syncSaleModelConfigFromBuildConfigs(model.getSaleModelCode(), SecurityContextHolder.getUserId());

        // 重新同步SaleModelBaseModel
        syncBaseModelFromBuildConfigs(model.getSaleModelCode(), SecurityContextHolder.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfigSort(Long saleModelId, SaleModelConfigSortDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        String saleModelCode = model.getSaleModelCode();

        if (dto.getFamilies() != null) {
            for (SaleModelConfigSortDto.FamilySortItem familyItem : dto.getFamilies()) {
                SaleModelConfigPo familyPo = saleModelConfigRepository.findById(familyItem.getFamilyId())
                        .orElse(null);
                if (familyPo != null && familyPo.getSaleModelCode().equals(saleModelCode)) {
                    familyPo.setSort(familyItem.getSort());
                    familyPo.setModifyBy(userId);
                    saleModelConfigRepository.update(familyPo);

                    if (familyItem.getFeatures() != null) {
                        for (SaleModelConfigSortDto.FeatureSortItem featureItem : familyItem.getFeatures()) {
                            SaleModelConfigPo featurePo = saleModelConfigRepository.findById(featureItem.getFeatureId())
                                    .orElse(null);
                            if (featurePo != null && featurePo.getSaleModelCode().equals(saleModelCode)) {
                                featurePo.setSort(featureItem.getSort());
                                featurePo.setModifyBy(userId);
                                saleModelConfigRepository.update(featurePo);
                            }
                        }
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncSaleModelConfigFromBuildConfigs(String saleModelCode, String userId) {
        log.info("开始同步销售车型配置，saleModelCode: {}", saleModelCode);
        List<SaleModelBuildConfigPo> buildConfigs = saleModelBuildConfigRepository.findBySaleModelCode(saleModelCode);
        log.info("找到 {} 个生产配置关联", buildConfigs.size());

        List<SaleModelConfigPo> existingConfigs = saleModelConfigRepository.findBySaleModelCode(saleModelCode);
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
                        .saleModelCode(saleModelCode)
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
                log.info("新增特征族: saleModelCode={}, type={}, typeName={}", saleModelCode, familyCode, familyName);
            } else {
                if (!existingFamily.getEnable()) {
                    existingFamily.setEnable(true);
                    existingFamily.setTypeName(familyName);
                    existingFamily.setModifyBy(userId);
                    saleModelConfigRepository.update(existingFamily);
                    log.info("重新启用特征族: saleModelCode={}, type={}, typeName={}", saleModelCode, familyCode, familyName);
                } else {
                    log.info("特征族已存在且启用: saleModelCode={}, type={}", saleModelCode, familyCode);
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
.saleModelCode(saleModelCode)
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
                    log.info("新增特征值: saleModelCode={}, type={}, typeCode={}, typeName={}", saleModelCode, familyCode, featureCode, typeName);
                } else {
                    if (!existing.getEnable()) {
                        existing.setEnable(true);
                        existing.setTypeName(typeName);
                        existing.setModifyBy(userId);
                        saleModelConfigRepository.update(existing);
                        log.info("重新启用特征值: saleModelCode={}, type={}, typeCode={}, typeName={}", saleModelCode, familyCode, featureCode, typeName);
                    } else {
                        log.info("特征值已存在且启用: saleModelCode={}, type={}, typeCode={}", saleModelCode, familyCode, featureCode);
                    }
                }
            }
        }

        log.info("待清理的配置项数量: {}", toDelete.size());
        for (String key : toDelete) {
            SaleModelConfigPo config = existingMap.get(key);
            if (config != null) {
                saleModelConfigRepository.physicalDeleteBySaleModelCodeAndIds(saleModelCode, new Long[]{config.getId()});
                String itemType = config.getTypeCode() == null ? "特征族" : "特征值";
                log.info("物理删除{}: saleModelCode={}, type={}, typeCode={}", itemType, saleModelCode, config.getType(), config.getTypeCode());
            }
        }

        log.info("同步完成，特征族 {} 个，特征值 {} 个，删除 {} 个", featureValueMap.size(),
                featureValueMap.values().stream().mapToInt(Set::size).sum(), toDelete.size());
    }

    public List<SaleModelBaseModelVo> getBaseModelList(Long saleModelId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        List<SaleModelBaseModelPo> poList = saleModelBaseModelRepository.findBySaleModelCode(model.getSaleModelCode());
        return poList.stream().map(po -> {
            SaleModelBaseModelVo vo = SaleModelBaseModelVo.builder()
                    .id(po.getId())
                    .saleModelCode(po.getSaleModelCode())
                    .baseModelCode(po.getBaseModelCode())
                    .baseModelName(po.getBaseModelName())
                    .baseModelImage(parseJsonToList(po.getBaseModelImage()))
                    .baseModelPrice(po.getBaseModelPrice())
                    .baseModelDesc(po.getBaseModelDesc())
                    .baseModelParam(po.getBaseModelParam())
                    .enable(po.getEnable())
                    .sort(po.getSort())
                    .createTime(po.getCreateTime() != null ? po.getCreateTime().toInstant() : null)
                    .createBy(po.getCreateBy())
                    .modifyTime(po.getModifyTime() != null ? po.getModifyTime().toInstant() : null)
                    .modifyBy(po.getModifyBy())
                    .build();
            return vo;
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBaseModel(Long saleModelId, SaleModelBaseModelDto dto, String userId) {
        SaleModelResult model = getSaleModelById(saleModelId);
        SaleModelBaseModelPo po = saleModelBaseModelRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("基础车型关联不存在"));
        if (!po.getSaleModelCode().equals(model.getSaleModelCode())) {
            throw new IllegalArgumentException("基础车型关联不属于该销售车型");
        }
        po.setBaseModelName(dto.getBaseModelName());
        po.setBaseModelImage(dto.getBaseModelImage() != null ? cn.hutool.json.JSONUtil.toJsonStr(dto.getBaseModelImage()) : null);
        po.setBaseModelPrice(dto.getBaseModelPrice());
        po.setBaseModelDesc(dto.getBaseModelDesc());
        po.setBaseModelParam(dto.getBaseModelParam());
        po.setEnable(dto.getEnable());
        po.setSort(dto.getSort());
        po.setModifyBy(userId);
        saleModelBaseModelRepository.update(po);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncBaseModelFromBuildConfigs(String saleModelCode, String userId) {
        log.info("开始同步基础车型，saleModelCode: {}", saleModelCode);
        List<SaleModelBuildConfigPo> buildConfigs = saleModelBuildConfigRepository.findBySaleModelCode(saleModelCode);
        log.info("找到 {} 个生产配置关联", buildConfigs.size());

        List<SaleModelBaseModelPo> existingBaseModels = saleModelBaseModelRepository.findBySaleModelCode(saleModelCode);
        Map<String, SaleModelBaseModelPo> existingMap = existingBaseModels.stream()
                .collect(Collectors.toMap(SaleModelBaseModelPo::getBaseModelCode, c -> c));

        Map<String, String> baseModelNameMap = new HashMap<>();
        Map<String, Integer> baseModelSortMap = new HashMap<>();
        int sort = 0;

        for (SaleModelBuildConfigPo bc : buildConfigs) {
            log.info("处理生产配置关联: buildConfigCode={}, enable={}", bc.getBuildConfigCode(), bc.getEnable());
            if (!bc.getEnable()) continue;

            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(bc.getBuildConfigCode());
            if (buildConfig != null && buildConfig.getBaseModelCode() != null) {
                String baseModelCode = buildConfig.getBaseModelCode();
                baseModelNameMap.putIfAbsent(baseModelCode, buildConfig.getName());
                baseModelSortMap.putIfAbsent(baseModelCode, sort++);
                log.info("提取基础车型: baseModelCode={}, name={}", baseModelCode, buildConfig.getName());
            }
        }

        log.info("聚合后的基础车型数量: {}", baseModelNameMap.size());

        Set<String> toDelete = new HashSet<>(existingMap.keySet());
        toDelete.removeAll(baseModelNameMap.keySet());

        for (Map.Entry<String, String> entry : baseModelNameMap.entrySet()) {
            String baseModelCode = entry.getKey();
            String baseModelName = entry.getValue();
            Integer baseModelSort = baseModelSortMap.get(baseModelCode);

            toDelete.remove(baseModelCode);

            SaleModelBaseModelPo existing = existingMap.get(baseModelCode);
            if (existing == null) {
                SaleModelBaseModelPo newPo = SaleModelBaseModelPo.builder()
                        .saleModelCode(saleModelCode)
                        .baseModelCode(baseModelCode)
                        .baseModelName(baseModelName)
                        .baseModelImage(null)
                        .baseModelPrice(BigDecimal.ZERO)
                        .baseModelDesc("")
                        .baseModelParam("")
                        .enable(true)
                        .sort(baseModelSort)
                        .rowValid(true)
                        .rowVersion(0)
                        .createBy(userId)
                        .modifyBy(userId)
                        .build();
                saleModelBaseModelRepository.insert(newPo);
                log.info("新增基础车型: saleModelCode={}, baseModelCode={}, baseModelName={}", saleModelCode, baseModelCode, baseModelName);
            } else {
                if (!existing.getEnable()) {
                    existing.setEnable(true);
                    if (existing.getBaseModelName() == null || existing.getBaseModelName().isEmpty()) {
                        existing.setBaseModelName(baseModelName);
                    }
                    existing.setSort(baseModelSort);
                    existing.setModifyBy(userId);
                    saleModelBaseModelRepository.update(existing);
                    log.info("重新启用基础车型: saleModelCode={}, baseModelCode={}, baseModelName={}", saleModelCode, baseModelCode, baseModelName);
                } else {
                    if (existing.getBaseModelName() == null || existing.getBaseModelName().isEmpty()) {
                        existing.setBaseModelName(baseModelName);
                    }
                    existing.setSort(baseModelSort);
                    existing.setModifyBy(userId);
                    saleModelBaseModelRepository.update(existing);
                    log.info("更新基础车型排序: saleModelCode={}, baseModelCode={}", saleModelCode, baseModelCode);
                }
            }
        }

        log.info("待清理的基础车型数量: {}", toDelete.size());
        for (String baseModelCode : toDelete) {
            SaleModelBaseModelPo po = existingMap.get(baseModelCode);
            if (po != null) {
                saleModelBaseModelRepository.physicalDeleteByIds(new Long[]{po.getId()});
                log.info("物理删除基础车型: saleModelCode={}, baseModelCode={}", saleModelCode, baseModelCode);
            }
        }

        log.info("同步完成，基础车型 {} 个，删除 {} 个", baseModelNameMap.size(), toDelete.size());
    }

    /**
     * 获取 Configuration 白名单
     */
    public List<SaleModelConfigPolicyPo> getConfigPolicies(String saleModelCode) {
        return configPolicyRepository.findBySaleModelCode(saleModelCode);
    }

    /**
     * 获取可用的 Configuration 列表（MDM 投影 + 白名单状态）
     *
     * @param saleModelCode 销售车型编码
     * @return MDM 投影中的 Configuration 列表，标注是否在白名单
     */
    public List<ConfigPolicyAvailableVo> getAvailableConfigPolicies(String saleModelCode) {
        // 获取销售车型信息
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode)
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModelCode));

        String variantCode = saleModel.getVariantCode();
        if (variantCode == null || variantCode.isEmpty()) {
            return List.of();
        }

        // 获取 MDM 投影中的 Configuration 列表
        List<MdmProjectionConfigurationPo> mdmConfigs = mdmProjectionService.getConfigurationsByVariant(variantCode);

        // 获取白名单列表
        List<SaleModelConfigPolicyPo> whitelist = configPolicyRepository.findBySaleModelCode(saleModelCode);
        Map<String, SaleModelConfigPolicyPo> whitelistMap = whitelist.stream()
            .collect(Collectors.toMap(SaleModelConfigPolicyPo::getConfigurationCode, po -> po));

        // 合并结果
        List<ConfigPolicyAvailableVo> result = new ArrayList<>();
        for (MdmProjectionConfigurationPo mdmConfig : mdmConfigs) {
            SaleModelConfigPolicyPo policy = whitelistMap.get(mdmConfig.getConfigurationCode());
            ConfigPolicyAvailableVo vo = ConfigPolicyAvailableVo.builder()
                .configurationCode(mdmConfig.getConfigurationCode())
                .variantCode(mdmConfig.getVariantCode())
                .optionCodes(mdmConfig.getOptionCodes() != null ?
                    cn.hutool.json.JSONUtil.toList(mdmConfig.getOptionCodes(), String.class) : List.of())
                .guidePrice(mdmConfig.getGuidePrice())
                .inWhitelist(policy != null)
                .policyStatus(policy != null ? policy.getStatus() : null)
                .build();
            result.add(vo);
        }

        return result;
    }

    /**
     * 获取可用的 OptionCode 列表（按 OptionFamily 分组）
     * 用于销售策略页展示可选 OptionCode 列表
     *
     * @param saleModelCode 销售车型编码
     * @return MDM 投影中的 OptionCode 列表（按 OptionFamily 分组），标注是否已有销售策略
     */
    public List<OptionFamilyAvailableVo> getAvailableOptionPolicies(String saleModelCode) {
        // 获取销售车型信息
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode)
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModelCode));

        String variantCode = saleModel.getVariantCode();
        if (variantCode == null || variantCode.isEmpty()) {
            return List.of();
        }

        // 获取所有 OptionFamily
        List<MdmProjectionOptionFamilyPo> families = mdmProjectionService.getAllOptionFamilies();

        // 获取该销售车型的销售策略列表
        List<SaleModelOptionPolicyPo> policies = optionPolicyRepository.findBySaleModelCode(saleModelCode);
        Map<String, SaleModelOptionPolicyPo> policyMap = policies.stream()
            .collect(Collectors.toMap(SaleModelOptionPolicyPo::getOptionCode, po -> po));

        // 获取该销售车型的 OptionFamily 销售策略
        List<SaleModelOptionFamilyPolicyPo> familyPolicies = optionFamilyPolicyRepository.findBySaleModelCode(saleModelCode);
        Map<String, SaleModelOptionFamilyPolicyPo> familyPolicyMap = familyPolicies.stream()
            .collect(Collectors.toMap(SaleModelOptionFamilyPolicyPo::getOptionFamilyCode, po -> po));

        // 按 OptionFamily 分组构建结果
        List<OptionFamilyAvailableVo> result = new ArrayList<>();
        for (MdmProjectionOptionFamilyPo family : families) {
            // 获取该 OptionFamily 下的所有 OptionCode
            List<MdmProjectionOptionPo> options = mdmProjectionService.getOptionsByOptionFamily(family.getOptionFamilyCode());
            if (options.isEmpty()) {
                continue;
            }

            // 构建该 OptionFamily 下的 OptionCode 列表
            List<OptionFamilyAvailableVo.OptionAvailableVo> optionVos = new ArrayList<>();
            for (MdmProjectionOptionPo option : options) {
                SaleModelOptionPolicyPo policy = policyMap.get(option.getOptionCode());
                OptionFamilyAvailableVo.OptionAvailableVo optionVo = OptionFamilyAvailableVo.OptionAvailableVo.builder()
                    .optionCode(option.getOptionCode())
                    .optionName(option.getOptionName())
                    .inPolicy(policy != null)
                    .saleStatus(policy != null ? policy.getSaleStatus() : null)
                    .optionPrice(policy != null ? policy.getOptionPrice() : null)
                    .build();
                optionVos.add(optionVo);
            }

            // 获取自定义的 OptionFamily 营销信息
            SaleModelOptionFamilyPolicyPo familyPolicy = familyPolicyMap.get(family.getOptionFamilyCode());

            OptionFamilyAvailableVo familyVo = OptionFamilyAvailableVo.builder()
                .optionFamilyCode(family.getOptionFamilyCode())
                .optionFamilyName(family.getOptionFamilyName())
                .marketingTitle(familyPolicy != null ? familyPolicy.getMarketingTitle() : null)
                .marketingImage(familyPolicy != null ? familyPolicy.getMarketingImage() : null)
                .marketingDesc(familyPolicy != null ? familyPolicy.getMarketingDesc() : null)
                .options(optionVos)
                .build();
            result.add(familyVo);
        }

        return result;
    }

    /**
     * 创建 Configuration 白名单
     */
    @Transactional(rollbackFor = Exception.class)
    public List<SaleModelConfigPolicyPo> createConfigPolicy(CreateConfigPolicyCmd cmd) {
        List<SaleModelConfigPolicyPo> result = new ArrayList<>();
        // 去重
        Set<String> uniqueConfigCodes = new LinkedHashSet<>(cmd.getConfigurationCodes());

        for (String configurationCode : uniqueConfigCodes) {
            // 检查是否已存在
            Optional<SaleModelConfigPolicyPo> existing = configPolicyRepository.findBySaleModelCodeAndConfigCode(
                cmd.getSaleModelCode(), configurationCode);

            if (existing.isPresent()) {
                SaleModelConfigPolicyPo po = existing.get();
                if (!Boolean.TRUE.equals(po.getRowValid())) {
                    String newStatus = cmd.getStatus() != null ? cmd.getStatus() : po.getStatus();
                    configPolicyRepository.reactivate(po.getId(), newStatus);
                    po.setRowValid(true);
                    po.setStatus(newStatus);
                } else if (cmd.getStatus() != null && !cmd.getStatus().equals(po.getStatus())) {
                    po.setStatus(cmd.getStatus());
                    po.setModifyTime(new java.sql.Timestamp(System.currentTimeMillis()));
                    configPolicyRepository.update(po);
                }
                result.add(po);
            } else {
                // 不存在则创建
                SaleModelConfigPolicyPo po = SaleModelConfigPolicyPo.builder()
                    .saleModelCode(cmd.getSaleModelCode())
                    .configurationCode(configurationCode)
                    .status(cmd.getStatus() != null ? cmd.getStatus() : "active")
                    .build();
                po.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                po.setModifyTime(new java.sql.Timestamp(System.currentTimeMillis()));
                configPolicyRepository.save(po);
                result.add(po);
            }
        }
        return result;
    }

    /**
     * 删除 Configuration 白名单
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteConfigPolicy(String saleModelCode, String configurationCode) {
        SaleModelConfigPolicyPo policy = configPolicyRepository
            .findBySaleModelCodeAndConfigCode(saleModelCode, configurationCode)
            .orElse(null);
        if (policy != null) {
            configPolicyRepository.delete(policy.getId());
            return 1;
        }
        return 0;
    }

    /**
     * 获取 OptionCode 销售策略
     */
    public PageResult<SaleModelOptionPolicyPo> getOptionPolicies(String saleModelCode, String optionFamilyCode, String saleStatus, Integer page, Integer size) {
        List<SaleModelOptionPolicyPo> policies = optionPolicyRepository.findBySaleModelCode(saleModelCode);

        if (optionFamilyCode != null && !optionFamilyCode.isEmpty()) {
            policies = policies.stream()
                .filter(p -> optionFamilyCode.equals(p.getOptionFamilyCode()))
                .collect(Collectors.toList());
        }

        if (saleStatus != null && !saleStatus.isEmpty()) {
            policies = policies.stream()
                .filter(p -> saleStatus.equals(p.getSaleStatus()))
                .collect(Collectors.toList());
        }

        int total = policies.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<SaleModelOptionPolicyPo> pageData = fromIndex < total ?
            policies.subList(fromIndex, toIndex) : new ArrayList<>();

        return PageResult.<SaleModelOptionPolicyPo>builder()
            .total(total)
            .items(pageData)
            .page(page)
            .size(size)
            .build();
    }

    /**
     * 创建 OptionCode 销售策略
     */
    @Transactional(rollbackFor = Exception.class)
    public SaleModelOptionPolicyPo createOptionPolicy(CreateOptionPolicyCmd cmd) {
        SaleModelOptionPolicyPo po = SaleModelOptionPolicyPo.builder()
            .saleModelCode(cmd.getSaleModelCode())
            .optionCode(cmd.getOptionCode())
            .optionFamilyCode(cmd.getOptionFamilyCode())
            .saleStatus(cmd.getSaleStatus() != null ? cmd.getSaleStatus() : "active")
            .optionPrice(cmd.getOptionPrice())
            .availableRegions(cmd.getAvailableRegions() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getAvailableRegions()) : null)
            .channels(cmd.getChannels() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getChannels()) : null)
            .bundleWith(cmd.getBundleWith() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getBundleWith()) : null)
            .mutexWith(cmd.getMutexWith() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getMutexWith()) : null)
            .marketingTitle(cmd.getMarketingTitle())
            .marketingImage(cmd.getMarketingImage())
            .sortWeight(cmd.getSortWeight())
            .build();
        po.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
        po.setModifyTime(new java.sql.Timestamp(System.currentTimeMillis()));
        optionPolicyRepository.save(po);
        return po;
    }

    /**
     * 更新 OptionCode 销售策略
     */
    @Transactional(rollbackFor = Exception.class)
    public SaleModelOptionPolicyPo updateOptionPolicy(Long id, CreateOptionPolicyCmd cmd) {
        SaleModelOptionPolicyPo po = optionPolicyRepository.findBySaleModelCodeAndOptionCode(cmd.getSaleModelCode(), cmd.getOptionCode())
            .orElseThrow(() -> new IllegalArgumentException("销售策略不存在"));

        po.setOptionFamilyCode(cmd.getOptionFamilyCode());
        po.setSaleStatus(cmd.getSaleStatus());
        po.setOptionPrice(cmd.getOptionPrice());
        po.setAvailableRegions(cmd.getAvailableRegions() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getAvailableRegions()) : null);
        po.setChannels(cmd.getChannels() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getChannels()) : null);
        po.setBundleWith(cmd.getBundleWith() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getBundleWith()) : null);
        po.setMutexWith(cmd.getMutexWith() != null ? cn.hutool.json.JSONUtil.toJsonStr(cmd.getMutexWith()) : null);
        po.setMarketingTitle(cmd.getMarketingTitle());
        po.setMarketingImage(cmd.getMarketingImage());
        po.setSortWeight(cmd.getSortWeight());

        optionPolicyRepository.update(po);
        return po;
    }

    /**
     * 删除 OptionCode 销售策略
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteOptionPolicy(Long id) {
        optionPolicyRepository.delete(id);
        return 1;
    }

    /**
     * 获取 OptionFamily 销售策略列表
     */
    public List<SaleModelOptionFamilyPolicyPo> getOptionFamilyPolicies(String saleModelCode) {
        return optionFamilyPolicyRepository.findBySaleModelCode(saleModelCode);
    }

    /**
     * 创建/更新 OptionFamily 销售策略
     */
    @Transactional(rollbackFor = Exception.class)
    public List<SaleModelOptionFamilyPolicyPo> createOptionFamilyPolicy(CreateOptionFamilyPolicyCmd cmd) {
        List<SaleModelOptionFamilyPolicyPo> result = new ArrayList<>();

        // 支持单个 optionFamilyCode 或批量 optionFamilyCodes
        List<String> familyCodes = new ArrayList<>();
        if (cmd.getOptionFamilyCode() != null && !cmd.getOptionFamilyCode().isEmpty()) {
            familyCodes.add(cmd.getOptionFamilyCode());
        }
        if (cmd.getOptionFamilyCodes() != null && !cmd.getOptionFamilyCodes().isEmpty()) {
            familyCodes.addAll(cmd.getOptionFamilyCodes());
        }
        if (familyCodes.isEmpty()) {
            return result;
        }
        Set<String> uniqueFamilyCodes = new LinkedHashSet<>(familyCodes);

        for (String optionFamilyCode : uniqueFamilyCodes) {
            Optional<SaleModelOptionFamilyPolicyPo> existing = optionFamilyPolicyRepository.findBySaleModelCodeAndFamilyCode(
                cmd.getSaleModelCode(), optionFamilyCode);

            if (existing.isPresent()) {
                SaleModelOptionFamilyPolicyPo po = existing.get();
                boolean needUpdate = false;
                if (cmd.getMarketingTitle() != null && !cmd.getMarketingTitle().equals(po.getMarketingTitle())) {
                    po.setMarketingTitle(cmd.getMarketingTitle());
                    needUpdate = true;
                }
                if (cmd.getMarketingImage() != null && !cmd.getMarketingImage().equals(po.getMarketingImage())) {
                    po.setMarketingImage(cmd.getMarketingImage());
                    needUpdate = true;
                }
                if (cmd.getMarketingDesc() != null && !cmd.getMarketingDesc().equals(po.getMarketingDesc())) {
                    po.setMarketingDesc(cmd.getMarketingDesc());
                    needUpdate = true;
                }
                if (cmd.getSortWeight() != null && !cmd.getSortWeight().equals(po.getSortWeight())) {
                    po.setSortWeight(cmd.getSortWeight());
                    needUpdate = true;
                }
                if (needUpdate) {
                    optionFamilyPolicyRepository.update(po);
                }
                result.add(po);
            } else {
                SaleModelOptionFamilyPolicyPo po = SaleModelOptionFamilyPolicyPo.builder()
                    .saleModelCode(cmd.getSaleModelCode())
                    .optionFamilyCode(optionFamilyCode)
                    .marketingTitle(cmd.getMarketingTitle())
                    .marketingImage(cmd.getMarketingImage())
                    .marketingDesc(cmd.getMarketingDesc())
                    .sortWeight(cmd.getSortWeight() != null ? cmd.getSortWeight() : 0)
                    .build();
                po.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
                po.setModifyTime(new java.sql.Timestamp(System.currentTimeMillis()));
                optionFamilyPolicyRepository.save(po);
                result.add(po);
            }
        }
        return result;
    }

    /**
     * 删除 OptionFamily 销售策略
     */
    @Transactional(rollbackFor = Exception.class)
    public int deleteOptionFamilyPolicy(String saleModelCode, String optionFamilyCode) {
        SaleModelOptionFamilyPolicyPo policy = optionFamilyPolicyRepository
            .findBySaleModelCodeAndFamilyCode(saleModelCode, optionFamilyCode)
            .orElse(null);
        if (policy != null) {
            optionFamilyPolicyRepository.delete(policy.getId());
            return 1;
        }
        return 0;
    }
}
