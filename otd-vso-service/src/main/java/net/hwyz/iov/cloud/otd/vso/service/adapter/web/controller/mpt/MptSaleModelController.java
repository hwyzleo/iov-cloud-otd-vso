package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelConfigMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelConfigVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.SaleModelQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mpt/saleModel/v1")
public class MptSaleModelController extends BaseController {

    private final SaleModelAppService saleModelAppService;

    @GetMapping("/list")
    public ApiResponse<PageResult<SaleModelVo>> list(
            @RequestParam(required = false) String saleModelCode,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Long beginTime,
            @RequestParam(required = false) Long endTime) {
        Instant begin = beginTime != null ? Instant.ofEpochSecond(beginTime) : null;
        Instant end = endTime != null ? Instant.ofEpochSecond(endTime) : null;
        startPage();
        List<SaleModelResult> result = saleModelAppService.search(SaleModelQuery.builder()
                .saleModelCode(saleModelCode)
                .modelName(modelName)
                .beginTime(begin)
                .endTime(end)
                .build());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, SaleModelVoAssembler.INSTANCE::toVo)));
    }

    @GetMapping("/{id}")
    public ApiResponse<SaleModelVo> get(@PathVariable Long id) {
        SaleModelResult result = saleModelAppService.getSaleModelById(id);
        return ApiResponse.ok(SaleModelVoAssembler.INSTANCE.toVo(result));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody SaleModelCreateDto dto) {
        return ApiResponse.ok(saleModelAppService.createSaleModel(dto, SecurityUtils.getUserId().toString()));
    }

    @PutMapping
    public ApiResponse<Void> update(@RequestBody SaleModelUpdateDto dto) {
        saleModelAppService.modifySaleModel(dto, SecurityUtils.getUserId().toString());
        return ApiResponse.ok();
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam Long[] ids) {
        saleModelAppService.deleteSaleModelByIds(ids);
        return ApiResponse.ok();
    }

    @GetMapping("/{saleModelId}/config")
    public ApiResponse<List<SaleModelConfigFamilyVo>> listConfigs(@PathVariable Long saleModelId) {
        List<SaleModelConfigFamilyVo> resultList = saleModelAppService.getSaleModelConfigFamilyList(saleModelId);
        return ApiResponse.ok(resultList);
    }

    @GetMapping("/{saleModelId}/config/{configId}")
    public ApiResponse<SaleModelConfigVo> getConfig(@PathVariable Long saleModelId, @PathVariable Long configId) {
        SaleModelConfigResult result = saleModelAppService.getSaleModelConfigById(saleModelId, configId);
        return ApiResponse.ok(SaleModelConfigVoAssembler.INSTANCE.toVo(result));
    }

    @PutMapping("/{saleModelId}/config")
    public ApiResponse<Void> updateConfig(@PathVariable Long saleModelId,
                                          @RequestBody SaleModelConfigDto dto) {
        saleModelAppService.modifySaleModelConfig(saleModelId, dto, SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @GetMapping("/{saleModelCode}/config/map")
    public ApiResponse<java.util.Map<String, SaleModelConfigMpt>> getConfigMap(@PathVariable String saleModelCode) {
        return ApiResponse.ok(SaleModelConfigMptAssembler.INSTANCE.toVoMap(saleModelAppService.getSaleModelConfigMap(saleModelCode)));
    }

    @GetMapping("/{saleModelId}/buildConfig")
    public ApiResponse<PageResult<SaleModelBuildConfigVo>> listBuildConfigs(@PathVariable Long saleModelId) {
        SaleModelResult model = saleModelAppService.getSaleModelById(saleModelId);
        startPage();
        List<SaleModelBuildConfigVo> result = saleModelAppService.getBuildConfigPageBySaleCode(model.getSaleModelCode());
        return ApiResponse.ok(getPageResult(result));
    }

    @GetMapping("/{saleModelId}/featureCodeRanges")
    public ApiResponse<List<FeatureCodeRangeVo>> getFeatureCodeRanges(@PathVariable Long saleModelId) {
        return ApiResponse.ok(saleModelAppService.getAggregatedFeatureCodeRanges(saleModelId));
    }

@PostMapping("/{saleModelId}/buildConfig")
    public ApiResponse<List<Long>> createBuildConfig(@PathVariable Long saleModelId,
                                                 @RequestBody SaleModelBuildConfigDto dto) {
        return ApiResponse.ok(saleModelAppService.batchCreateBuildConfig(saleModelId, dto, SecurityUtils.getUserId().toString()));
    }

    @PutMapping("/{saleModelId}/buildConfig")
    public ApiResponse<Void> updateBuildConfig(@PathVariable Long saleModelId,
                                                @RequestBody SaleModelBuildConfigDto dto) {
        saleModelAppService.updateBuildConfig(saleModelId, dto, SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{saleModelId}/buildConfig")
    public ApiResponse<Void> deleteBuildConfigs(@PathVariable Long saleModelId, @RequestParam String ids) {
        Long[] idArray = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toArray(Long[]::new);
        saleModelAppService.deleteBuildConfig(saleModelId, idArray);
        return ApiResponse.ok();
    }

    @PostMapping("/{saleModelId}/syncConfigs")
    public ApiResponse<Void> syncConfigs(@PathVariable Long saleModelId) {
        SaleModelResult model = saleModelAppService.getSaleModelById(saleModelId);
        saleModelAppService.syncSaleModelConfigFromBuildConfigs(model.getSaleModelCode(), SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @GetMapping("/{saleModelId}/baseModel")
    public ApiResponse<List<SaleModelBaseModelVo>> listBaseModels(@PathVariable Long saleModelId) {
        return ApiResponse.ok(saleModelAppService.getBaseModelList(saleModelId));
    }

    @PutMapping("/{saleModelId}/baseModel")
    public ApiResponse<Void> updateBaseModel(@PathVariable Long saleModelId,
                                             @RequestBody SaleModelBaseModelDto dto) {
        saleModelAppService.updateBaseModel(saleModelId, dto, SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @PostMapping("/{saleModelId}/syncBaseModels")
    public ApiResponse<Void> syncBaseModels(@PathVariable Long saleModelId) {
        SaleModelResult model = saleModelAppService.getSaleModelById(saleModelId);
        saleModelAppService.syncBaseModelFromBuildConfigs(model.getSaleModelCode(), SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @PutMapping("/{saleModelId}/config/sort")
    public ApiResponse<Void> updateConfigSort(@PathVariable Long saleModelId,
                                              @RequestBody SaleModelConfigSortDto dto) {
        saleModelAppService.batchUpdateConfigSort(saleModelId, dto, SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }
}
