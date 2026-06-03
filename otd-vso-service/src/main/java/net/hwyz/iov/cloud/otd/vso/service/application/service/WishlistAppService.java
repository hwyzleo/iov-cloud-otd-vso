package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.mdm.api.service.ConfigurationService;
import net.hwyz.iov.cloud.edd.mdm.api.vo.request.ConfigurationByVariantAndOptionCodesRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ConfigurationNotMatchedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DuplicateWishlistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.SaleModelNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistLimitExceededException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.WishlistInvalidReason;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelModelPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.MdmProjectionService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.SalesPolicyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 心愿单应用服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistAppService {

    private final WishlistRepository wishlistRepository;
    private final SaleModelRepository saleModelRepository;
    private final SaleModelModelPolicyRepository modelPolicyRepository;
    private final SaleModelVariantPolicyRepository variantPolicyRepository;
    private final SaleModelOptionPolicyRepository optionPolicyRepository;
    private final SalesPolicyService salesPolicyService;
    private final MdmProjectionService mdmProjectionService;
    private final ConfigurationService configurationService;

    @Transactional(rollbackFor = Exception.class)
    public String createWishlist(CreateWishlistCmd cmd) {
        log.info("创建心愿单：accountId={}, saleModelCode={}, modelCode={}, variantCode={}, optionCodes={}",
                cmd.getAccountId(), cmd.getSaleModelCode(), cmd.getModelCode(),
                cmd.getVariantCode(), cmd.getOptionCodes());

        validateWishlistLimit(cmd.getAccountId());

        // 五项校验 + resolveConfiguration
        String configurationCode = validateAndResolveConfiguration(
                cmd.getSaleModelCode(), cmd.getModelCode(), cmd.getVariantCode(), cmd.getOptionCodes());

        // 唯一性校验
        validateDuplicateWishlist(cmd.getAccountId(), cmd.getSaleModelCode(), cmd.getModelCode(),
                cmd.getVariantCode(), configurationCode, cmd.getOptionCodes(), null);

        Wishlist wishlist = Wishlist.create(cmd.getAccountId(), cmd.getSaleModelCode(),
                cmd.getModelCode(), cmd.getVariantCode(), configurationCode, cmd.getOptionCodes());
        wishlistRepository.save(wishlist);
        log.info("心愿单创建成功：wishlistId={}, configurationCode={}", wishlist.getId(), configurationCode);
        return wishlist.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyWishlist(ModifyWishlistCmd cmd) {
        log.info("修改心愿单：wishlistId={}, accountId={}, modelCode={}, variantCode={}, optionCodes={}",
                cmd.getWishlistId(), cmd.getAccountId(), cmd.getModelCode(),
                cmd.getVariantCode(), cmd.getOptionCodes());

        Wishlist wishlist = findWishlistById(cmd.getAccountId(), cmd.getWishlistId());

        // 五项校验 + resolveConfiguration（saleModelCode 取原心愿单的）
        String configurationCode = validateAndResolveConfiguration(
                wishlist.getSaleModelCode(), cmd.getModelCode(), cmd.getVariantCode(), cmd.getOptionCodes());

        // 唯一性校验（排除当前心愿单）
        validateDuplicateWishlist(cmd.getAccountId(), wishlist.getSaleModelCode(), cmd.getModelCode(),
                cmd.getVariantCode(), configurationCode, cmd.getOptionCodes(), cmd.getWishlistId());

        wishlist.modify(cmd.getModelCode(), cmd.getVariantCode(), configurationCode, cmd.getOptionCodes());
        wishlistRepository.save(wishlist);
        log.info("心愿单修改成功：wishlistId={}", wishlist.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWishlist(DeleteWishlistCmd cmd) {
        log.info("删除心愿单：wishlistId={}, accountId={}", cmd.getWishlistId(), cmd.getAccountId());
        Wishlist wishlist = findWishlistById(cmd.getAccountId(), cmd.getWishlistId());
        wishlist.delete();
        wishlistRepository.save(wishlist);
        log.info("心愿单删除成功：wishlistId={}", wishlist.getId());
    }

    public List<WishlistListResult> getWishlistList(String accountId) {
        log.info("获取心愿单列表：accountId={}", accountId);
        List<Wishlist> wishlists = wishlistRepository.findByUserId(accountId);
        return wishlists.stream()
                .map(wishlist -> {
                    refreshWishlistStatus(wishlist);
                    return toWishlistListResult(wishlist);
                })
                .collect(Collectors.toList());
    }

    public WishlistDetailResult getWishlistDetail(String accountId, String wishlistId) {
        log.info("获取心愿单详情：wishlistId={}, accountId={}", wishlistId, accountId);
        Wishlist wishlist = findWishlistById(accountId, wishlistId);
        refreshWishlistStatus(wishlist);
        return toWishlistDetailResult(wishlist);
    }

    private Wishlist findWishlistById(String accountId, String wishlistId) {
        return wishlistRepository.findByWishlistIdAndUserId(wishlistId, accountId)
                .orElseThrow(() -> new WishlistNotExistException(wishlistId));
    }

    private WishlistListResult toWishlistListResult(Wishlist wishlist) {
        WishlistListResult result = WishlistListResult.builder()
                .wishlistId(wishlist.getId())
                .saleModelCode(wishlist.getSaleModelCode())
                .modelCode(wishlist.getModelCode())
                .variantCode(wishlist.getVariantCode())
                .configurationCode(wishlist.getConfigurationCode())
                .optionCodes(wishlist.getOptionCodes())
                .createTime(wishlist.getCreateTime())
                .modifyTime(wishlist.getModifyTime())
                .invalidReason(wishlist.getInvalidReason())
                .build();

        enrichDisplayInfo(result);
        return result;
    }

    private WishlistDetailResult toWishlistDetailResult(Wishlist wishlist) {
        WishlistDetailResult result = WishlistDetailResult.builder()
                .wishlistId(wishlist.getId())
                .saleModelCode(wishlist.getSaleModelCode())
                .modelCode(wishlist.getModelCode())
                .variantCode(wishlist.getVariantCode())
                .configurationCode(wishlist.getConfigurationCode())
                .optionCodes(wishlist.getOptionCodes())
                .createTime(wishlist.getCreateTime())
                .modifyTime(wishlist.getModifyTime())
                .invalidReason(wishlist.getInvalidReason())
                .build();

        enrichDisplayInfo(result);
        return result;
    }

    /**
     * 实时校验心愿单状态，更新 invalidReason
     * 五项校验：① SaleModel 在售 ② Model 在售 ③ Variant 在售 ④ Configuration 白名单 ⑤ Option 在售
     */
    private void refreshWishlistStatus(Wishlist wishlist) {
        WishlistInvalidReason reason = checkWishlistInvalidReason(wishlist);
        String currentReason = wishlist.getInvalidReason();
        String newReason = reason != null ? reason.getCode() : null;

        if (currentReason == null && newReason == null) {
            return;
        }
        if (currentReason != null && currentReason.equals(newReason)) {
            return;
        }

        if (newReason != null) {
            wishlist.markInvalid(newReason);
            log.info("心愿单失效：wishlistId={}, reason={}", wishlist.getId(), newReason);
        } else {
            wishlist.clearInvalid();
            log.info("心愿单恢复有效：wishlistId={}", wishlist.getId());
        }
        wishlistRepository.save(wishlist);
    }

    /**
     * 逐项校验心愿单，返回第一个失效原因（null 表示全部有效）
     */
    private WishlistInvalidReason checkWishlistInvalidReason(Wishlist wishlist) {
        String saleModelCode = wishlist.getSaleModelCode();

        // ① SaleModel 在售（listingStatus = active，时间窗有效）
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode).orElse(null);
        if (saleModel == null || !"active".equals(saleModel.getListingStatus())) {
            return WishlistInvalidReason.SALE_MODEL_OFF_SHELF;
        }
        if (saleModel.getEffectiveFrom() != null && saleModel.getEffectiveTo() != null) {
            Timestamp now = Timestamp.from(Instant.now());
            if (now.before(saleModel.getEffectiveFrom()) || now.after(saleModel.getEffectiveTo())) {
                return WishlistInvalidReason.SALE_MODEL_OFF_SHELF;
            }
        }

        // ② Model 在售
        WishlistInvalidReason modelReason = salesPolicyService.checkModelForSale(saleModelCode, wishlist.getModelCode());
        if (modelReason != null) {
            return modelReason;
        }

        // ③ Variant 在售
        WishlistInvalidReason variantReason = salesPolicyService.checkVariantForSale(saleModelCode, wishlist.getVariantCode());
        if (variantReason != null) {
            return variantReason;
        }

        // ④ Configuration 白名单
        WishlistInvalidReason configReason = salesPolicyService.checkConfigurationForSale(saleModelCode, wishlist.getConfigurationCode());
        if (configReason != null) {
            return configReason;
        }

        // ⑤ Option 在售
        WishlistInvalidReason optionReason = salesPolicyService.checkOptionsForSale(saleModelCode, wishlist.getOptionCodes());
        if (optionReason != null) {
            return optionReason;
        }

        return null;
    }

    /**
     * 五项校验 + resolveConfiguration
     * ① SaleModel 存在 ② Model 在售 ③ Variant 在售 ④ resolveConfiguration ⑤ Option 在售
     *
     * @return 解析出的 configurationCode
     */
    private String validateAndResolveConfiguration(String saleModelCode, String modelCode,
                                                   String variantCode, List<String> optionCodes) {
        // ① 校验 SaleModel 存在
        SaleModelPo saleModel = saleModelRepository.findBySaleModelCode(saleModelCode)
            .orElseThrow(() -> new SaleModelNotExistException("销售车型不存在: " + saleModelCode));

        // ② Model 销售策略校验（空表全开）
        salesPolicyService.validateModelForSale(saleModelCode, modelCode);

        // ③ Variant 销售策略校验（空表全开，但 variantPrice 必须非空）
        salesPolicyService.validateVariantForSale(saleModelCode, variantCode);

        // ④ OptionCode 销售策略校验
        if (optionCodes != null && !optionCodes.isEmpty()) {
            salesPolicyService.validateOptionsForSale(saleModelCode, optionCodes, null);
        }

        // ⑤ MDM resolveConfiguration(variantCode, optionCodes) → configurationCode
        String configurationCode = resolveConfiguration(variantCode, optionCodes);
        if (configurationCode == null || configurationCode.isEmpty()) {
            throw new ConfigurationNotMatchedException(saleModelCode);
        }

        // ⑥ Configuration 销售白名单校验（空白名单视为 ALL 全开）
        salesPolicyService.validateConfigurationForSale(saleModelCode, configurationCode);

        return configurationCode;
    }

    /**
     * 调用 MDM 服务，根据 variantCode + optionCodes 反查 configurationCode
     */
    private String resolveConfiguration(String variantCode, List<String> optionCodes) {
        try {
            ConfigurationByVariantAndOptionCodesRequest request = ConfigurationByVariantAndOptionCodesRequest.builder()
                    .variantCode(variantCode)
                    .optionCodes(optionCodes)
                    .build();
            String configCode = configurationService.resolveConfiguration(request);
            log.debug("resolveConfiguration: variantCode={}, optionCodes={} -> configurationCode={}",
                    variantCode, optionCodes, configCode);
            return configCode;
        } catch (Exception e) {
            log.error("调用 MDM resolveConfiguration 失败: variantCode={}, optionCodes={}",
                    variantCode, optionCodes, e);
            return null;
        }
    }

    private static final int WISHLIST_LIMIT = 5;

    private void validateWishlistLimit(String userId) {
        long count = wishlistRepository.countByUserId(userId);
        if (count >= WISHLIST_LIMIT) {
            throw new WishlistLimitExceededException(WISHLIST_LIMIT);
        }
    }

    /**
     * 唯一性校验
     * 唯一键: (userId, saleModelCode, modelCode, variantCode, configurationCode, optionCodesHash)
     */
    private void validateDuplicateWishlist(String userId, String saleModelCode, String modelCode,
                                           String variantCode, String configurationCode,
                                           List<String> optionCodes, String excludeWishlistId) {
        String optionCodesHash = calculateOptionCodesHash(optionCodes);

        boolean exists;
        if (excludeWishlistId == null || excludeWishlistId.isEmpty()) {
            exists = wishlistRepository.existsByUniqueKey(userId, saleModelCode, modelCode,
                    variantCode, configurationCode, optionCodesHash);
        } else {
            exists = wishlistRepository.existsByUniqueKeyExcluding(userId, saleModelCode, modelCode,
                    variantCode, configurationCode, optionCodesHash, excludeWishlistId);
        }
        if (exists) {
            throw new DuplicateWishlistException(userId);
        }
    }

    /**
     * 计算 optionCodes 排序后的哈希值
     */
    private String calculateOptionCodesHash(List<String> optionCodes) {
        if (optionCodes == null || optionCodes.isEmpty()) {
            return "";
        }
        List<String> sorted = new java.util.ArrayList<>(optionCodes);
        java.util.Collections.sort(sorted);
        String joined = String.join(",", sorted);
        return cn.hutool.crypto.digest.DigestUtil.md5Hex(joined);
    }

    /**
     * 组装心愿单展示信息：displayName、saleModelDesc、saleModelImages、totalPrice、optionDetails
     */
    private void enrichDisplayInfo(WishlistListResult result) {
        try {
            // 1. 销售车型名称
            String saleModelName = saleModelRepository.findBySaleModelCode(result.getSaleModelCode())
                    .map(SaleModelPo::getModelName)
                    .orElse("");

            // 2. 车型销售名称（SaleModelModelPolicyPo.marketingName）
            String modelMarketingName = modelPolicyRepository
                    .findBySaleModelCodeAndModelCode(result.getSaleModelCode(), result.getModelCode())
                    .map(p -> p.getMarketingName() != null ? p.getMarketingName() : "")
                    .orElse("");

            // 3. 版本销售名称和价格
            String variantMarketingName = "";
            BigDecimal variantPrice = BigDecimal.ZERO;
            SaleModelVariantPolicyPo variantPolicy = variantPolicyRepository
                    .findBySaleModelCodeAndVariantCode(result.getSaleModelCode(), result.getVariantCode())
                    .orElse(null);
            if (variantPolicy != null) {
                variantMarketingName = variantPolicy.getMarketingName() != null ? variantPolicy.getMarketingName() : "";
                variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
            }

            // 4. displayName = 销售车型名称 + 车型销售名称 + 版本销售名称
            result.setDisplayName((saleModelName + " " + modelMarketingName + " " + variantMarketingName).trim());

            // 5. 获取用户选择的 Option 信息
            List<String> optionCodes = result.getOptionCodes();
            if (optionCodes != null && !optionCodes.isEmpty()) {
                List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyRepository
                        .findBySaleModelCodeAndOptionCodes(result.getSaleModelCode(), optionCodes);

                // saleModelDesc = option 营销名称拼接
                String desc = optionPolicies.stream()
                        .map(p -> p.getMarketingTitle() != null ? p.getMarketingTitle() : "")
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.joining(" "));
                result.setSaleModelDesc(desc);

                // saleModelImages = option 营销图片数组
                List<String> images = optionPolicies.stream()
                        .map(SaleModelOptionPolicyPo::getMarketingImage)
                        .filter(img -> img != null && !img.isEmpty())
                        .collect(Collectors.toList());
                result.setSaleModelImages(images);

                // optionDetails = 选项详情列表
                List<WishlistListResult.OptionDetail> optionDetails = optionPolicies.stream()
                        .map(p -> WishlistListResult.OptionDetail.builder()
                                .optionFamilyCode(p.getOptionFamilyCode())
                                .optionCode(p.getOptionCode())
                                .marketingTitle(p.getMarketingTitle())
                                .optionPrice(p.getOptionPrice())
                                .marketingImage(p.getMarketingImage())
                                .build())
                        .collect(Collectors.toList());
                result.setOptionDetails(optionDetails);

                // option 总价
                BigDecimal optionTotalPrice = optionPolicies.stream()
                        .map(p -> p.getOptionPrice() != null ? p.getOptionPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // totalPrice = variantPrice + Σ(optionPrice)
                result.setTotalPrice(variantPrice.add(optionTotalPrice));
            } else {
                result.setSaleModelDesc("");
                result.setSaleModelImages(Collections.emptyList());
                result.setOptionDetails(Collections.emptyList());
                result.setTotalPrice(variantPrice);
            }
        } catch (Exception e) {
            log.warn("组装心愿单展示信息失败: wishlistId={}", result.getWishlistId(), e);
        }
    }

    /**
     * 组装心愿单详情展示信息
     */
    private void enrichDisplayInfo(WishlistDetailResult result) {
        try {
            // 销售车型名称
            String saleModelName = saleModelRepository.findBySaleModelCode(result.getSaleModelCode())
                    .map(SaleModelPo::getModelName)
                    .orElse("");
            result.setSaleModelName(saleModelName);

            // 车型营销名称
            String modelMarketingName = modelPolicyRepository
                    .findBySaleModelCodeAndModelCode(result.getSaleModelCode(), result.getModelCode())
                    .map(p -> p.getMarketingName() != null ? p.getMarketingName() : "")
                    .orElse("");
            result.setModelMarketingName(modelMarketingName);

            // 版本营销名称和价格
            String variantMarketingName = "";
            BigDecimal variantPrice = BigDecimal.ZERO;
            SaleModelVariantPolicyPo variantPolicy = variantPolicyRepository
                    .findBySaleModelCodeAndVariantCode(result.getSaleModelCode(), result.getVariantCode())
                    .orElse(null);
            if (variantPolicy != null) {
                variantMarketingName = variantPolicy.getMarketingName() != null ? variantPolicy.getMarketingName() : "";
                variantPrice = variantPolicy.getVariantPrice() != null ? variantPolicy.getVariantPrice() : BigDecimal.ZERO;
            }
            result.setVariantMarketingName(variantMarketingName);
            result.setVariantPrice(variantPrice);

            List<String> optionCodes = result.getOptionCodes();
            if (optionCodes != null && !optionCodes.isEmpty()) {
                List<SaleModelOptionPolicyPo> optionPolicies = optionPolicyRepository
                        .findBySaleModelCodeAndOptionCodes(result.getSaleModelCode(), optionCodes);

                List<String> images = optionPolicies.stream()
                        .map(SaleModelOptionPolicyPo::getMarketingImage)
                        .filter(img -> img != null && !img.isEmpty())
                        .collect(Collectors.toList());
                result.setSaleModelImages(images);

                List<WishlistListResult.OptionDetail> optionDetails = optionPolicies.stream()
                        .map(p -> WishlistListResult.OptionDetail.builder()
                                .optionFamilyCode(p.getOptionFamilyCode())
                                .optionCode(p.getOptionCode())
                                .marketingTitle(p.getMarketingTitle())
                                .optionPrice(p.getOptionPrice())
                                .marketingImage(p.getMarketingImage())
                                .build())
                        .collect(Collectors.toList());
                result.setOptionDetails(optionDetails);

                BigDecimal optionTotalPrice = optionPolicies.stream()
                        .map(p -> p.getOptionPrice() != null ? p.getOptionPrice() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                result.setTotalPrice(variantPrice.add(optionTotalPrice));
            } else {
                result.setSaleModelImages(Collections.emptyList());
                result.setOptionDetails(Collections.emptyList());
                result.setTotalPrice(variantPrice);
            }
        } catch (Exception e) {
            log.warn("组装心愿单详情展示信息失败: wishlistId={}", result.getWishlistId(), e);
        }
    }

}
