package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateConfigPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionFamilyPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateOptionPolicyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mpt/saleModel/v1")
@RequiredArgsConstructor
public class MptSalesPolicyController {

    private final SaleModelAppService saleModelAppService;

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
