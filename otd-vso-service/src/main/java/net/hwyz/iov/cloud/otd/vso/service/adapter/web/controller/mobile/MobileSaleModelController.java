package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelConfigMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SelectedSaleModelAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetConfiguratorCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GetQuoteCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ConfiguratorResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.QuoteResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelConfigResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售车型相关手机接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/mobile/saleModel/v1")
public class MobileSaleModelController extends BaseController {

    private final SaleModelAppService saleModelAppService;

    /**
     * 获取销售车型列表
     *
     * @return 销售车型列表
     */
    @GetMapping("")
    public ApiResponse<List<SaleModelMp>> getSaleModelList() {
        log.info("手机客户端[{}]获取销售车型列表", ParamHelper.getClientAccountInfo());
        List<SaleModelResult> resultList = saleModelAppService.getSaleModelList();
        return ApiResponse.ok(SaleModelMpAssembler.INSTANCE.toVoList(resultList));
    }

    /**
     * 获取销售车型信息
     *
     * @param saleModelCode 销售车型代码
     * @return 销售车型信息
     */
    @GetMapping("/{saleModelCode}")
    public ApiResponse<SaleModelMp> getSaleModel(@PathVariable("saleModelCode") String saleModelCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]销售车型信息", ParamHelper.getClientAccountInfo(), saleModelCode);
        SaleModelResult result = saleModelAppService.getSaleModelByCode(saleModelCode);
        return ApiResponse.ok(SaleModelMpAssembler.INSTANCE.toVo(result));
    }

    /**
     * 获取销售车型配置列表
     *
     * @param saleModelCode 销售车型代码
     * @return 销售车型配置列表
     */
    @GetMapping("/{saleModelCode}/config")
    public ApiResponse<List<SaleModelConfigMp>> getSaleModelConfigList(@PathVariable("saleModelCode") String saleModelCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]销售车型配置列表", ParamHelper.getClientAccountInfo(), saleModelCode);
        List<SaleModelConfigResult> resultList = saleModelAppService.getSaleModelConfigList(saleModelCode);
        return ApiResponse.ok(SaleModelConfigMpAssembler.INSTANCE.toVoList(resultList));
    }

    /**
     * 获取销售车型可选特征值范围（动态配置模式）
     *
     * @param saleModelCode 销售车型代码
     * @return 特征值范围列表
     */
    @GetMapping("/{saleModelCode}/featureCodeRanges")
    public ApiResponse<List<FeatureCodeRangeVo>> getFeatureCodeRanges(@PathVariable("saleModelCode") String saleModelCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]可选特征值范围", ParamHelper.getClientAccountInfo(), saleModelCode);
        SaleModelResult model = saleModelAppService.getSaleModelByCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.getAggregatedFeatureCodeRanges(model.getId()));
    }

    /**
     * 根据选择的特征值获取销售车型信息（动态配置模式）
     *
     * @param requestVo 包含saleModelCode和选择的特征值Map
     * @return 已选择的销售车型及配置
     */
    @PostMapping("/selectedSaleModel")
    public ApiResponse<SelectedSaleModel> getSelectedSaleModel(@RequestBody SelectedSaleModelRequestVo requestVo) {
        log.info("手机客户端[{}]根据特征值获取销售车型信息", ParamHelper.getClientAccountInfo());
        return ApiResponse.ok(SelectedSaleModelAssembler.INSTANCE.toVo(
                saleModelAppService.getSelectedSaleModelByFeatureCodes(requestVo.getSaleModelCode(), requestVo.getSaleModelConfigType())));
    }

    /**
     * 获取销售车型购车权益
     *
     * @param saleModelCode 销售车型代码
     * @return 销售车型购车权益
     */
    @GetMapping("/purchaseBenefits/{saleModelCode}")
    public ApiResponse<PurchaseBenefits> getPurchaseBenefits(@PathVariable("saleModelCode") String saleModelCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]销售车型购车权益", ParamHelper.getClientAccountInfo(), saleModelCode);
        return ApiResponse.ok(saleModelAppService.getPurchaseBenefits(saleModelCode));
    }

    /**
     * 获取上牌区域列表
     *
     * @return 销售区域列表
     */
    @GetMapping("/licenseArea")
    public ApiResponse<List<LicenseArea>> getLicenseAreaList() {
        log.info("手机客户端[{}]获取上牌区域列表", ParamHelper.getClientAccountInfo());
        return ApiResponse.ok(saleModelAppService.getLicenseAreaList());
    }

    /**
     * 获取选配器数据
     *
     * @param saleModelCode 销售车型代码
     * @param regionCode 区域代码
     * @return 选配器数据
     */
    @GetMapping("/{saleModelCode}/configurator")
    public ApiResponse<ConfiguratorResult> getConfigurator(
            @PathVariable String saleModelCode,
            @RequestParam String regionCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]选配器数据, 区域[{}]", ParamHelper.getClientAccountInfo(), saleModelCode, regionCode);
        GetConfiguratorCmd cmd = GetConfiguratorCmd.builder()
            .saleModelCode(saleModelCode)
            .regionCode(regionCode)
            .build();
        return ApiResponse.ok(saleModelAppService.getConfigurator(cmd));
    }

    /**
     * 获取实时报价
     *
     * @param cmd 报价请求参数
     * @return 报价结果
     */
    @PostMapping("/quote")
    public ApiResponse<QuoteResult> getQuote(@RequestBody GetQuoteCmd cmd) {
        log.info("手机客户端[{}]获取实时报价, 销售车型[{}]", ParamHelper.getClientAccountInfo(), cmd.getSaleModelCode());
        return ApiResponse.ok(saleModelAppService.getQuote(cmd));
    }
}
