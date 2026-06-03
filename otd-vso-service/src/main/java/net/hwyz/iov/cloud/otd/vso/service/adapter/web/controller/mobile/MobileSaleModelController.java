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
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.SaleModelPriceResult;
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
     * 获取销售车型价格信息（起售价、意向金、首付）
     * 基于 Variant 销售策略实时计算
     *
     * @param saleModelCode 销售车型代码
     * @param regionCode 区域代码（可选）
     * @return 价格信息
     */
    @GetMapping("/{saleModelCode}/prices")
    public ApiResponse<SaleModelPriceResult> getSaleModelPrices(
            @PathVariable("saleModelCode") String saleModelCode,
            @RequestParam(value = "regionCode", required = false) String regionCode) {
        log.info("手机客户端[{}]获取销售车型代码[{}]价格信息, 区域[{}]", ParamHelper.getClientAccountInfo(), saleModelCode, regionCode);
        return ApiResponse.ok(saleModelAppService.getSaleModelPrices(saleModelCode, regionCode));
    }

    /**
     * @deprecated 使用 /configurator 接口替代
     * 获取销售车型配置列表
     */
    @Deprecated
    @GetMapping("/{saleModelCode}/config")
    public ApiResponse<List<SaleModelConfigMp>> getSaleModelConfigList(@PathVariable("saleModelCode") String saleModelCode) {
        log.warn("手机客户端[{}]使用已废弃的接口获取配置列表, saleModelCode: {}", ParamHelper.getClientAccountInfo(), saleModelCode);
        List<SaleModelConfigResult> resultList = saleModelAppService.getSaleModelConfigList(saleModelCode);
        return ApiResponse.ok(SaleModelConfigMpAssembler.INSTANCE.toVoList(resultList));
    }

    /**
     * @deprecated 使用 /configurator 接口替代
     * 获取销售车型可选特征值范围（动态配置模式）
     */
    @Deprecated
    @GetMapping("/{saleModelCode}/featureCodeRanges")
    public ApiResponse<List<FeatureCodeRangeVo>> getFeatureCodeRanges(@PathVariable("saleModelCode") String saleModelCode) {
        log.warn("手机客户端[{}]使用已废弃的接口获取特征值范围, saleModelCode: {}", ParamHelper.getClientAccountInfo(), saleModelCode);
        SaleModelResult model = saleModelAppService.getSaleModelByCode(saleModelCode);
        return ApiResponse.ok(saleModelAppService.getAggregatedFeatureCodeRanges(model.getId()));
    }

    /**
     * @deprecated 使用 /configurator + /quote 接口替代
     * 根据选择的特征值获取销售车型信息（动态配置模式）
     */
    @Deprecated
    @PostMapping("/selectedSaleModel")
    public ApiResponse<SelectedSaleModel> getSelectedSaleModel(@RequestBody SelectedSaleModelRequestVo requestVo) {
        log.warn("手机客户端[{}]使用已废弃的接口获取已选车型信息, saleModelCode: {}", ParamHelper.getClientAccountInfo(), requestVo.getSaleModelCode());
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
     * 订单总价 = variantPrice + Σ(optionPrice)
     *
     * @param cmd 报价请求参数
     * @return 报价结果
     */
    @PostMapping("/quote")
    public ApiResponse<QuoteResult> getQuote(@RequestBody GetQuoteCmd cmd) {
        log.info("手机客户端[{}]获取实时报价, 销售车型[{}], Variant[{}]",
            ParamHelper.getClientAccountInfo(), cmd.getSaleModelCode(), cmd.getVariantCode());
        return ApiResponse.ok(saleModelAppService.getQuote(cmd));
    }
}
