package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.vmd.api.service.VmdVehicleModelConfigService;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigFeatureCodeResponse;
import net.hwyz.iov.cloud.edd.vmd.api.vo.response.VmdBuildConfigResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.WishlistDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SelectedSaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final SaleModelAppService saleModelAppService;
    private final VmdVehicleModelConfigService vmdVehicleModelConfigService;

    @Transactional(rollbackFor = Exception.class)
    public String createWishlist(CreateWishlistCmd cmd) {
        log.info("创建心愿单：accountId={}, saleCode={}, featureConfig={}", 
                cmd.getAccountId(), cmd.getSaleCode(), cmd.getFeatureConfig());
        
        SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                cmd.getSaleCode(), cmd.getFeatureConfig());
        
        String buildConfigCode = selectedModel.getBuildConfigCode();
        if (buildConfigCode == null || buildConfigCode.isEmpty()) {
            throw new IllegalArgumentException("无法匹配到有效的生产配置");
        }
        
        Wishlist wishlist = Wishlist.create(cmd.getAccountId(), cmd.getSaleCode(), buildConfigCode);
        wishlistRepository.save(wishlist);
        log.info("心愿单创建成功：wishlistId={}, buildConfigCode={}", wishlist.getId(), buildConfigCode);
        return wishlist.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyWishlist(ModifyWishlistCmd cmd) {
        log.info("修改心愿单：wishlistId={}, accountId={}, featureConfig={}", 
                cmd.getWishlistId(), cmd.getAccountId(), cmd.getFeatureConfig());
        
        Wishlist wishlist = findWishlistById(cmd.getAccountId(), cmd.getWishlistId());
        
        SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                wishlist.getSaleCode(), cmd.getFeatureConfig());
        
        String buildConfigCode = selectedModel.getBuildConfigCode();
        if (buildConfigCode == null || buildConfigCode.isEmpty()) {
            throw new IllegalArgumentException("无法匹配到有效的生产配置");
        }
        
        wishlist.modify(buildConfigCode);
        wishlistRepository.save(wishlist);
        log.info("心愿单修改成功：wishlistId={}, buildConfigCode={}", wishlist.getId(), buildConfigCode);
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
                .map(this::toWishlistListResult)
                .collect(Collectors.toList());
    }

    public WishlistDetailResult getWishlistDetail(String accountId, String wishlistId) {
        log.info("获取心愿单详情：wishlistId={}, accountId={}", wishlistId, accountId);
        Wishlist wishlist = findWishlistById(accountId, wishlistId);
        return toWishlistDetailResult(wishlist);
    }

    private Wishlist findWishlistById(String accountId, String wishlistId) {
        return wishlistRepository.findByWishlistIdAndUserId(wishlistId, accountId)
                .orElseThrow(() -> new WishlistNotExistException(wishlistId));
    }
    
    private WishlistListResult toWishlistListResult(Wishlist wishlist) {
        Map<String, String> featureCodes = parseBuildConfigToFeatureCodes(wishlist.getBuildConfigCode());
        SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                wishlist.getSaleCode(), featureCodes);
        
        return WishlistListResult.builder()
                .wishlistId(wishlist.getId())
                .saleCode(wishlist.getSaleCode())
                .buildConfigCode(wishlist.getBuildConfigCode())
                .createTime(wishlist.getCreateTime())
                .modifyTime(wishlist.getModifyTime())
                .saleModelConfigType(selectedModel.getSaleModelConfigType())
                .saleModelConfigName(selectedModel.getSaleModelConfigName())
                .saleModelImages(selectedModel.getSaleModelImages())
                .totalPrice(selectedModel.getTotalPrice())
                .isValid(checkBuildConfigValid(wishlist.getBuildConfigCode()))
                .build();
    }
    
    private WishlistDetailResult toWishlistDetailResult(Wishlist wishlist) {
        Map<String, String> featureCodes = parseBuildConfigToFeatureCodes(wishlist.getBuildConfigCode());
        SelectedSaleModelResult selectedModel = saleModelAppService.getSelectedSaleModelByFeatureCodes(
                wishlist.getSaleCode(), featureCodes);
        
        return WishlistDetailResult.builder()
                .wishlistId(wishlist.getId())
                .saleCode(wishlist.getSaleCode())
                .buildConfigCode(wishlist.getBuildConfigCode())
                .createTime(wishlist.getCreateTime())
                .modifyTime(wishlist.getModifyTime())
                .saleModelConfigType(selectedModel.getSaleModelConfigType())
                .saleModelConfigName(selectedModel.getSaleModelConfigName())
                .saleModelConfigPrice(selectedModel.getSaleModelConfigPrice())
                .saleModelImages(selectedModel.getSaleModelImages())
                .saleModelDesc(selectedModel.getSaleModelDesc())
                .totalPrice(selectedModel.getTotalPrice())
                .isValid(checkBuildConfigValid(wishlist.getBuildConfigCode()))
                .build();
    }
    
    private Map<String, String> parseBuildConfigToFeatureCodes(String buildConfigCode) {
        Map<String, String> featureCodes = new HashMap<>();
        
        try {
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
            
            if (buildConfig != null && buildConfig.getFeatureCodes() != null) {
                for (VmdBuildConfigFeatureCodeResponse fc : buildConfig.getFeatureCodes()) {
                    String familyCode = fc.getFamilyCode();
                    if (fc.getFeatureCode() != null && fc.getFeatureCode().length > 0) {
                        featureCodes.put(familyCode, fc.getFeatureCode()[0]);
                    }
                }
                
                if (buildConfig.getBaseModelCode() != null && !buildConfig.getBaseModelCode().isEmpty()) {
                    featureCodes.put("BASE_MODEL", buildConfig.getBaseModelCode());
                }
            }
        } catch (Exception e) {
            log.warn("解析生产配置失败: buildConfigCode={}", buildConfigCode, e);
        }
        
        return featureCodes;
    }
    
    private boolean checkBuildConfigValid(String buildConfigCode) {
        try {
            VmdBuildConfigResponse buildConfig = vmdVehicleModelConfigService.getBuildConfigByCode(buildConfigCode);
            return buildConfig != null && buildConfig.getEnable() != null && buildConfig.getEnable();
        } catch (Exception e) {
            log.warn("检查生产配置有效性失败: buildConfigCode={}", buildConfigCode, e);
            return false;
        }
    }

}