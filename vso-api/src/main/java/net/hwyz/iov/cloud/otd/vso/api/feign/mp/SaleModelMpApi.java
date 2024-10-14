package net.hwyz.iov.cloud.otd.vso.api.feign.mp;

import net.hwyz.iov.cloud.otd.vso.api.contract.PurchaseBenefits;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModel;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelConfig;
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
     * 获取销售车型购车权益
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型购车权益
     */
    Response<PurchaseBenefits> getPurchaseBenefits(String saleCode, ClientAccount clientAccount);

}
