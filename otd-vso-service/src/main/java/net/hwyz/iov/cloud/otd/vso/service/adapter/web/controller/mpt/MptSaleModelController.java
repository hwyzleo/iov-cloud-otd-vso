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
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mpt/saleModel/v1")
public class MptSaleModelController extends BaseController {

    private final SaleModelAppService saleModelAppService;

    @GetMapping("/list")
    public ApiResponse<PageResult<SaleModelVo>> list(
            @RequestParam(required = false) String saleCode,
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) Long beginTime,
            @RequestParam(required = false) Long endTime) {
        Instant begin = beginTime != null ? Instant.ofEpochSecond(beginTime) : null;
        Instant end = endTime != null ? Instant.ofEpochSecond(endTime) : null;
        startPage();
        List<SaleModelResult> result = saleModelAppService.search(SaleModelQuery.builder()
                .saleCode(saleCode)
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
    public ApiResponse<List<SaleModelConfigVo>> listConfigs(@PathVariable Long saleModelId) {
        List<SaleModelConfigResult> resultList = saleModelAppService.getSaleModelConfigList(saleModelId);
        return ApiResponse.ok(SaleModelConfigVoAssembler.INSTANCE.toVoList(resultList));
    }

    @PostMapping("/{saleModelId}/config")
    public ApiResponse<Long> createConfig(@PathVariable Long saleModelId,
                                          @RequestBody SaleModelConfigDto dto) {
        return ApiResponse.ok(saleModelAppService.createSaleModelConfig(saleModelId, dto, SecurityUtils.getUserId().toString()));
    }

    @PutMapping("/{saleModelId}/config")
    public ApiResponse<Void> updateConfig(@PathVariable Long saleModelId,
                                          @RequestBody SaleModelConfigDto dto) {
        saleModelAppService.modifySaleModelConfig(saleModelId, dto, SecurityContextHolder.getUserId());
        return ApiResponse.ok();
    }

    @DeleteMapping("/{saleModelId}/config")
    public ApiResponse<Void> deleteConfigs(@PathVariable Long saleModelId, @RequestParam Long[] ids) {
        saleModelAppService.deleteSaleModelConfigByIds(saleModelId, ids);
        return ApiResponse.ok();
    }

    @GetMapping("/{saleCode}/selected")
    public ApiResponse<SaleModelVo> getSelectedSaleModel(
            @PathVariable String saleCode,
            @RequestParam(required = false) String modelCode,
            @RequestParam(required = false) String exteriorCode,
            @RequestParam(required = false) String interiorCode,
            @RequestParam(required = false) String wheelCode,
            @RequestParam(required = false) String spareTireCode,
            @RequestParam(required = false) String adasCode) {
        return ApiResponse.ok(SaleModelVoAssembler.INSTANCE.toVo(saleModelAppService.getSelectedSaleModel(
                saleCode, modelCode, exteriorCode, interiorCode, wheelCode, spareTireCode, adasCode)));
    }

    @GetMapping("/{saleCode}/config/map")
    public ApiResponse<java.util.Map<String, SaleModelConfigMpt>> getConfigMap(@PathVariable String saleCode) {
        return ApiResponse.ok(SaleModelConfigMptAssembler.INSTANCE.toVoMap(saleModelAppService.getSaleModelConfigMap(saleCode)));
    }
}
