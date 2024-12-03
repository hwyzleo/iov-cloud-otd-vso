package net.hwyz.iov.cloud.otd.vso.service.facade.mp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ClientAccount;
import net.hwyz.iov.cloud.framework.common.bean.Response;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.otd.vso.api.contract.*;
import net.hwyz.iov.cloud.otd.vso.api.feign.mp.SaleModelMpApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelConfigMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelMpAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelConfigPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
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
@RequestMapping(value = "/mp/saleModel")
public class SaleModelMpController implements SaleModelMpApi {

    private final SaleModelAppService saleModelAppService;

    /**
     * 获取销售车型列表
     *
     * @param clientAccount 终端用户
     * @return 销售车型列表
     */
    @Override
    @GetMapping("")
    public Response<List<SaleModelMp>> getSaleModelList(@RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售车型列表", ParamHelper.getClientAccountInfo(clientAccount));
        List<SaleModelPo> saleModelPoList = saleModelAppService.getSaleModelList();
        return new Response<>(SaleModelMpAssembler.INSTANCE.fromPoList(saleModelPoList));
    }

    /**
     * 获取销售车型信息
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型信息
     */
    @Override
    @GetMapping("/{saleCode}")
    public Response<SaleModelMp> getSaleModel(@PathVariable("saleCode") String saleCode,
                                              @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售代码[{}]销售车型信息", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        SaleModelPo saleModelPo = saleModelAppService.getSaleModelByCode(saleCode);
        return new Response<>(SaleModelMpAssembler.INSTANCE.fromPo(saleModelPo));
    }

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型配置列表
     */
    @Override
    @GetMapping("/{saleCode}/config")
    public Response<List<SaleModelConfigMp>> getSaleModelConfigList(@PathVariable("saleCode") String saleCode,
                                                                    @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售代码[{}]销售车型配置列表", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        List<SaleModelConfigPo> saleModelConfigPoList = saleModelAppService.getSaleModelConfigList(saleCode);
        List<SaleModelConfigMp> saleModelConfigMpList = SaleModelConfigMpAssembler.INSTANCE.fromPoList(saleModelConfigPoList);
        return new Response<>(saleModelConfigMpList);
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
    @Override
    @GetMapping("/selectedSaleModel")
    public Response<SelectedSaleModel> getSelectedSaleModel(@RequestParam String saleCode, @RequestParam String modelCode,
                                                            @RequestParam String exteriorCode, @RequestParam String interiorCode,
                                                            @RequestParam String wheelCode, @RequestParam String spareTireCode,
                                                            @RequestParam String adasCode, @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取已选择的销售车型及配置", ParamHelper.getClientAccountInfo(clientAccount));
        return new Response<>(saleModelAppService.getSelectedSaleModel(saleCode, modelCode, exteriorCode, interiorCode,
                wheelCode, spareTireCode, adasCode));
    }

    /**
     * 获取销售车型购车权益
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型购车权益
     */
    @Override
    @GetMapping("/purchaseBenefits/{saleCode}")
    public Response<PurchaseBenefits> getPurchaseBenefits(@PathVariable("saleCode") String saleCode,
                                                          @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售代码[{}]销售车型购车权益", ParamHelper.getClientAccountInfo(clientAccount), saleCode);
        return new Response<>(saleModelAppService.getPurchaseBenefits(saleCode));
    }

    /**
     * 获取销售车型购车协议
     *
     * @param saleCode      销售代码
     * @param type          协议类型
     * @param clientAccount 终端用户
     * @return 销售车型购车协议
     */
    @Override
    @GetMapping("/purchaseAgreement")
    public Response<PurchaseAgreement> getPurchaseAgreement(@RequestParam String saleCode, @RequestParam Integer type,
                                                            @RequestHeader ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取销售代码[{}]销售车型购车协议[{}]", ParamHelper.getClientAccountInfo(clientAccount), saleCode, type);
        return new Response<>(saleModelAppService.getPurchaseAgreement(saleCode, type));
    }

    /**
     * 获取上牌区域列表
     *
     * @param clientAccount 终端用户
     * @return 销售区域列表
     */
    @Override
    @GetMapping("/licenseArea")
    public Response<List<LicenseArea>> getLicenseAreaList(ClientAccount clientAccount) {
        logger.info("手机客户端[{}]获取上牌区域列表", ParamHelper.getClientAccountInfo(clientAccount));
        return new Response<>(saleModelAppService.getLicenseAreaList());
    }
}
