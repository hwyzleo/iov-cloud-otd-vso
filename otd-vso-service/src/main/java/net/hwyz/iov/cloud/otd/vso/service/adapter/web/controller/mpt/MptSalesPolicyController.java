package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.ModelPolicyVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.VariantPolicyVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateConfigPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateModelPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionFamilyPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateVariantPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mpt/saleModel/v1")
@RequiredArgsConstructor
public class MptSalesPolicyController {

    private final SaleModelAppService saleModelAppService;

    // ==================== Model 销售策略 ====================

    /**
     * 获取 Model 销售策略列表
     * 返回该 carlineCode 下全部 Model 列表，标注是否已配置策略及 saleStatus
     */
    @GetMapping("/{saleModelCode}/modelPolicy")
    public ApiResponse<?> getModelPolicy(@PathVariable String saleModelCode) {
        return ApiResponse.ok(saleModelAppService.getModelPolicies(saleModelCode));
    }

    /**
     * 获取单个 Model 销售策略详情
     */
    @GetMapping("/{saleModelCode}/modelPolicy/{modelCode}")
    public ApiResponse<ModelPolicyVo> getModelPolicyDetail(
            @PathVariable String saleModelCode,
            @PathVariable String modelCode) {
        return ApiResponse.ok(saleModelAppService.getModelPolicy(saleModelCode, modelCode));
    }

    /**
     * 创建/更新 Model 销售策略
     */
    @PostMapping("/{saleModelCode}/modelPolicy")
    public ApiResponse<?> createModelPolicy(
            @PathVariable String saleModelCode,
            @RequestBody CreateModelPolicyCmd cmd) {
        cmd.setSaleModelCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.createModelPolicy(cmd));
    }

    /**
     * 删除 Model 销售策略
     */
    @DeleteMapping("/{saleModelCode}/modelPolicy/{modelCode}")
    public ApiResponse<?> deleteModelPolicy(
            @PathVariable String saleModelCode,
            @PathVariable String modelCode) {
        return ApiResponse.ok(saleModelAppService.deleteModelPolicy(saleModelCode, modelCode));
    }

    // ==================== Variant 销售策略 ====================

    /**
     * 获取 Variant 销售策略列表
     * 返回指定 Model 下全部 Variant 列表，标注是否已配置策略及价格/saleStatus
     */
    @GetMapping("/{saleModelCode}/variantPolicy")
    public ApiResponse<?> getVariantPolicy(
            @PathVariable String saleModelCode,
            @RequestParam(required = false) String modelCode) {
        return ApiResponse.ok(saleModelAppService.getVariantPolicies(saleModelCode, modelCode));
    }

    /**
     * 获取单个 Variant 销售策略详情
     */
    @GetMapping("/{saleModelCode}/variantPolicy/{variantCode}")
    public ApiResponse<VariantPolicyVo> getVariantPolicyDetail(
            @PathVariable String saleModelCode,
            @PathVariable String variantCode) {
        return ApiResponse.ok(saleModelAppService.getVariantPolicy(saleModelCode, variantCode));
    }

    /**
     * 创建/更新 Variant 销售策略
     */
    @PostMapping("/{saleModelCode}/variantPolicy")
    public ApiResponse<?> createVariantPolicy(
            @PathVariable String saleModelCode,
            @RequestBody CreateVariantPolicyCmd cmd) {
        cmd.setSaleModelCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.createVariantPolicy(cmd));
    }

    /**
     * 删除 Variant 销售策略
     */
    @DeleteMapping("/{saleModelCode}/variantPolicy/{variantCode}")
    public ApiResponse<?> deleteVariantPolicy(
            @PathVariable String saleModelCode,
            @PathVariable String variantCode) {
        return ApiResponse.ok(saleModelAppService.deleteVariantPolicy(saleModelCode, variantCode));
    }

    // ==================== Configuration 白名单 ====================

    /**
     * 获取 Configuration 白名单
     */
    @GetMapping("/{saleModelCode}/configPolicy")
    public ApiResponse<?> getConfigPolicy(@PathVariable String saleModelCode) {
        return ApiResponse.ok(saleModelAppService.getConfigPolicies(saleModelCode));
    }

    /**
     * 获取可用的 Configuration 列表（MDM 投影 + 白名单状态）
     * 用于销售策略页展示可选 Configuration 列表
     */
    @GetMapping("/{saleModelCode}/configPolicy/available")
    public ApiResponse<?> getAvailableConfigPolicies(@PathVariable String saleModelCode) {
        return ApiResponse.ok(saleModelAppService.getAvailableConfigPolicies(saleModelCode));
    }

    /**
     * 创建 Configuration 白名单
     */
    @PostMapping("/{saleModelCode}/configPolicy")
    public ApiResponse<?> createConfigPolicy(
            @PathVariable String saleModelCode,
            @RequestBody CreateConfigPolicyCmd cmd) {
        cmd.setSaleModelCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.createConfigPolicy(cmd));
    }

    /**
     * 删除 Configuration 白名单
     */
    @DeleteMapping("/{saleModelCode}/configPolicy/{configurationCode}")
    public ApiResponse<?> deleteConfigPolicy(
            @PathVariable String saleModelCode,
            @PathVariable String configurationCode) {
        return ApiResponse.ok(saleModelAppService.deleteConfigPolicy(saleModelCode, configurationCode));
    }

    /**
     * 获取 OptionCode 销售策略
     */
    @GetMapping("/{saleModelCode}/optionPolicy")
    public ApiResponse<?> getOptionPolicy(
            @PathVariable String saleModelCode,
            @RequestParam(required = false) String optionFamilyCode,
            @RequestParam(required = false) String saleStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ApiResponse.ok(saleModelAppService.getOptionPolicies(saleModelCode, optionFamilyCode, saleStatus, page, size));
    }

    /**
     * 获取 OptionCode 销售策略详情
     */
    @GetMapping("/{saleModelCode}/optionPolicy/{id}")
    public ApiResponse<?> getOptionPolicyById(
            @PathVariable String saleModelCode,
            @PathVariable Long id) {
        return ApiResponse.ok(saleModelAppService.getOptionPolicyById(id));
    }

    /**
     * 获取可用的 OptionCode 列表（按 OptionFamily 分组）
     * 用于销售策略页展示可选 OptionCode 列表
     */
    @GetMapping("/{saleModelCode}/optionPolicy/available")
    public ApiResponse<?> getAvailableOptionPolicies(@PathVariable String saleModelCode) {
        return ApiResponse.ok(saleModelAppService.getAvailableOptionPolicies(saleModelCode));
    }

    /**
     * 创建 OptionCode 销售策略
     */
    @PostMapping("/{saleModelCode}/optionPolicy")
    public ApiResponse<?> createOptionPolicy(
            @PathVariable String saleModelCode,
            @RequestBody CreateOptionPolicyCmd cmd) {
        cmd.setSaleModelCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.createOptionPolicy(cmd));
    }

    /**
     * 更新 OptionCode 销售策略
     */
    @PutMapping("/{saleModelCode}/optionPolicy/{id}")
    public ApiResponse<?> updateOptionPolicy(
            @PathVariable String saleModelCode,
            @PathVariable Long id,
            @RequestBody CreateOptionPolicyCmd cmd) {
        return ApiResponse.ok(saleModelAppService.updateOptionPolicy(id, cmd));
    }

    /**
     * 删除 OptionCode 销售策略
     */
    @DeleteMapping("/{saleModelCode}/optionPolicy/{id}")
    public ApiResponse<?> deleteOptionPolicy(
            @PathVariable String saleModelCode,
            @PathVariable Long id) {
        return ApiResponse.ok(saleModelAppService.deleteOptionPolicy(id));
    }

    /**
     * 获取 OptionFamily 销售策略
     */
    @GetMapping("/{saleModelCode}/optionFamilyPolicy")
    public ApiResponse<?> getOptionFamilyPolicy(@PathVariable String saleModelCode) {
        return ApiResponse.ok(saleModelAppService.getOptionFamilyPolicies(saleModelCode));
    }

    /**
     * 创建/更新 OptionFamily 销售策略
     */
    @PostMapping("/{saleModelCode}/optionFamilyPolicy")
    public ApiResponse<?> createOptionFamilyPolicy(
            @PathVariable String saleModelCode,
            @RequestBody CreateOptionFamilyPolicyCmd cmd) {
        cmd.setSaleModelCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.createOptionFamilyPolicy(cmd));
    }

    /**
     * 删除 OptionFamily 销售策略
     */
    @DeleteMapping("/{saleModelCode}/optionFamilyPolicy/{optionFamilyCode}")
    public ApiResponse<?> deleteOptionFamilyPolicy(
            @PathVariable String saleModelCode,
            @PathVariable String optionFamilyCode) {
        return ApiResponse.ok(saleModelAppService.deleteOptionFamilyPolicy(saleModelCode, optionFamilyCode));
    }
}
