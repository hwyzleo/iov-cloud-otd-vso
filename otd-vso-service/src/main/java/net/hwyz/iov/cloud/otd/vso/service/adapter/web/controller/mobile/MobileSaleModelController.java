package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.ClientAccount;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SaleModelConfigMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.SelectedSaleModelAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
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
     * @param clientAccount 终端用户
     * @return 销售车型列表
     */
    @GetMapping("")
    public ApiResponse<List<SaleModelMp>> getSaleModelList(@RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售车型列表", ParamHelper.getClientAccountInfo(clientAccount));
        List<SaleModelResult> resultList = saleModelAppService.getSaleModelList();
        return ApiResponse.ok(SaleModelMpAssembler.INSTANCE.toVoList(resultList));
    }

    /**
     * 获取销售车型信息
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型信息
     */
    @GetMapping("/{saleCode}")
    public ApiResponse<SaleModelMp> getSaleModel(@PathVariable("saleCode") String saleCode,
                                                 @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售代码[{}]销售车型信息", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        SaleModelResult result = saleModelAppService.getSaleModelByCode(saleCode);
        return ApiResponse.ok(SaleModelMpAssembler.INSTANCE.toVo(result));
    }

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型配置列表
     */
    @GetMapping("/{saleCode}/config")
    public ApiResponse<List<SaleModelConfigMp>> getSaleModelConfigList(@PathVariable("saleCode") String saleCode,
                                                                       @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售代码[{}]销售车型配置列表", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        List<SaleModelConfigResult> resultList = saleModelAppService.getSaleModelConfigList(saleCode);
        return ApiResponse.ok(SaleModelConfigMpAssembler.INSTANCE.toVoList(resultList));
    }

    /**
     * 获取销售车型可选特征值范围（动态配置模式）
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 特征值范围列表
     */
    @GetMapping("/{saleCode}/featureCodeRanges")
    public ApiResponse<List<FeatureCodeRangeVo>> getFeatureCodeRanges(@PathVariable("saleCode") String saleCode,
                                                                      @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售代码[{}]可选特征值范围", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        SaleModelResult model = saleModelAppService.getSaleModelByCode(saleCode);
        return ApiResponse.ok(saleModelAppService.getAggregatedFeatureCodeRanges(model.getId()));
    }

    /**
     * 根据选择的特征值获取销售车型信息（动态配置模式）
     *
     * @param requestVo     包含saleCode和选择的特征值Map
     * @param clientAccount 终端用户
     * @return 已选择的销售车型及配置
     */
    @PostMapping("/selectedSaleModel")
    public ApiResponse<SelectedSaleModel> getSelectedSaleModel(@RequestBody SelectedSaleModelRequestVo requestVo,
                                                               @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]根据特征值获取销售车型信息", ParamHelper.getClientAccountInfo(clientAccount));
        return ApiResponse.ok(SelectedSaleModelAssembler.INSTANCE.toVo(
                saleModelAppService.getSelectedSaleModelByFeatureCodes(requestVo.getSaleCode(), requestVo.getSaleModelConfigType())));
    }

    /**
     * 获取销售车型购车权益
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型购车权益
     */
    @GetMapping("/purchaseBenefits/{saleCode}")
    public ApiResponse<PurchaseBenefits> getPurchaseBenefits(@PathVariable("saleCode") String saleCode,
                                                             @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售代码[{}]销售车型购车权益", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        return ApiResponse.ok(saleModelAppService.getPurchaseBenefits(saleCode));
    }

    /**
     * 获取上牌区域列表
     *
     * @param clientAccount 终端用户
     * @return 销售区域列表
     */
    @GetMapping("/licenseArea")
    public ApiResponse<List<LicenseArea>> getLicenseAreaList(ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取上牌区域列表", ParamHelper.getClientAccountInfo(clientAccount));
        return ApiResponse.ok(saleModelAppService.getLicenseAreaList());
    }
}
