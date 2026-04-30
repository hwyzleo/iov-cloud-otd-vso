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
     * 获取已选择的销售车型及配置
     *
     * @param saleCode      销售代码
     * @param modelCode     车型代码
     * @param exteriorCode  外饰代码
     * @param interiorCode  内饰代码
     * @param wheelCode     车轮代码
     * @param spareTireCode 备胎代码
     * @param adasCode      智驾代码
     * @param clientAccount 终端用户
     * @return 已选择的销售车型及配置
     */
    @GetMapping("/selectedSaleModel")
    public ApiResponse<SelectedSaleModel> getSelectedSaleModel(@RequestParam String saleCode, @RequestParam String modelCode,
                                                               @RequestParam String exteriorCode, @RequestParam String interiorCode,
                                                               @RequestParam String wheelCode, @RequestParam String spareTireCode,
                                                               @RequestParam String adasCode, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取已选择的销售车型及配置", ParamHelper.getClientAccountInfo(clientAccount));
        return ApiResponse.ok(SelectedSaleModelAssembler.INSTANCE.toVo(saleModelAppService.getSelectedSaleModel(saleCode, modelCode, exteriorCode, interiorCode,
                wheelCode, spareTireCode, adasCode)));
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
     * 获取销售车型购车协议
     *
     * @param saleCode      销售代码
     * @param type          协议类型
     * @param clientAccount 终端用户
     * @return 销售车型购车协议
     */
    @GetMapping("/purchaseAgreement")
    public ApiResponse<PurchaseAgreement> getPurchaseAgreement(@RequestParam String saleCode, @RequestParam Integer type,
                                                               @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取销售代码[{}]销售车型购车协议[{}]", ParamHelper.getClientAccountInfo(clientAccount), saleCode, type);
        return ApiResponse.ok(saleModelAppService.getPurchaseAgreement(saleCode, type));
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
