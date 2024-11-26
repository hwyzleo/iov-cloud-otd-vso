package net.hwyz.iov.cloud.otd.vso.api.feign.mp;

import net.hwyz.iov.cloud.otd.vso.api.contract.*;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;

import java.util.List;

/**
 * 销售车型相关手机接口
 *
 * @author hwyz_leo
 */
public interface SaleModelMpApi {

    /**
     * 获取销售车型列表
     *
     * @param clientAccount 终端用户
     * @return 销售车型列表
     */
    Response<List<SaleModel>> getSaleModelList(ClientAccount clientAccount);

    /**
     * 获取销售车型信息
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型信息
     */
    Response<SaleModel> getSaleModel(String saleCode, ClientAccount clientAccount);

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型配置列表
     */
    Response<List<SaleModelConfig>> getSaleModelConfigList(String saleCode, ClientAccount clientAccount);

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
    Response<SelectedSaleModel> getSelectedSaleModel(String saleCode, String modelCode, String exteriorCode, String interiorCode,
                                                     String wheelCode, String spareTireCode, String adasCode, ClientAccount clientAccount);

    /**
     * 获取销售车型购车权益
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型购车权益
     */
    Response<PurchaseBenefits> getPurchaseBenefits(String saleCode, ClientAccount clientAccount);

    /**
     * 获取销售车型购车协议
     *
     * @param saleCode      销售代码
     * @param type          协议类型
     * @param clientAccount 终端用户
     * @return 销售车型购车协议
     */
    Response<PurchaseAgreement> getPurchaseAgreement(String saleCode, Integer type, ClientAccount clientAccount);

    /**
     * 获取上牌区域列表
     *
     * @param clientAccount 终端用户
     * @return 销售区域列表
     */
    Response<List<LicenseArea>> getLicenseAreaList(ClientAccount clientAccount);

}
