package net.hwyz.iov.cloud.otd.vso.api.feign.mp;

import net.hwyz.iov.cloud.otd.vso.api.contract.response.SaleModelResponse;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.ClientAccount;
import net.hwyz.iov.cloud.tsp.framework.commons.bean.Response;

/**
 * 销售车型相关手机接口
 *
 * @author hwyz_leo
 */
public interface SaleModelMpApi {

    /**
     * 获取销售车型配置列表
     *
     * @param saleCode      销售代码
     * @param clientAccount 终端用户
     * @return 销售车型配置列表
     */
    Response<SaleModelResponse> getSaleModelConfigList(String saleCode, ClientAccount clientAccount);

}
