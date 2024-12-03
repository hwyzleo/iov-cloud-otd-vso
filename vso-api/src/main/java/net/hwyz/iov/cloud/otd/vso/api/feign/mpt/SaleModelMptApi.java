package net.hwyz.iov.cloud.otd.vso.api.feign.mpt;

import jakarta.servlet.http.HttpServletResponse;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelConfigMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelMpt;

/**
 * 销售车型相关管理后台接口
 *
 * @author hwyz_leo
 */
public interface SaleModelMptApi {

    /**
     * 分页查询销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 销售车型信息列表
     */
    TableDataInfo list(SaleModelMpt saleModel);

    /**
     * 导出销售车型信息
     *
     * @param response  响应
     * @param saleModel 销售车型信息
     */
    void export(HttpServletResponse response, SaleModelMpt saleModel);

    /**
     * 根据销售车型ID获取销售车型信息
     *
     * @param saleModelId 销售车型ID
     * @return 销售车型信息
     */
    AjaxResult getInfo(Long saleModelId);

    /**
     * 根据销售车型ID查询销售车型配置信息
     *
     * @param saleModelId 销售车型ID
     * @return 销售车型配置信息列表
     */
    TableDataInfo listConfigBySaleCode(Long saleModelId);

    /**
     * 根据销售车型配置ID获取销售车型配置信息
     *
     * @param saleModelId       销售车型ID
     * @param saleModelConfigId 销售车型配置ID
     * @return 销售车型配置信息
     */
    AjaxResult getConfig(Long saleModelId, Long saleModelConfigId);

    /**
     * 新增销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    AjaxResult add(SaleModelMpt saleModel);

    /**
     * 新增销售车型配置信息
     *
     * @param saleModelId     销售车型ID
     * @param saleModelConfig 销售车型配置信息
     * @return 结果
     */
    AjaxResult addConfig(Long saleModelId, SaleModelConfigMpt saleModelConfig);

    /**
     * 修改保存销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    AjaxResult edit(SaleModelMpt saleModel);

    /**
     * 修改保存销售车型配置信息
     *
     * @param saleModelId     销售车型ID
     * @param saleModelConfig 销售车型配置信息
     * @return 结果
     */
    AjaxResult editConfig(Long saleModelId, SaleModelConfigMpt saleModelConfig);

    /**
     * 修改保存销售车型图片集
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    AjaxResult editImages(SaleModelMpt saleModel);

    /**
     * 删除销售车型信息
     *
     * @param saleModelIds 销售车型ID数组
     * @return 结果
     */
    AjaxResult remove(Long[] saleModelIds);

    /**
     * 删除销售车型配置信息
     *
     * @param saleModelId        销售车型ID
     * @param saleModelConfigIds 销售车型配置ID数组
     * @return 结果
     */
    AjaxResult remove(Long saleModelId, Long[] saleModelConfigIds);

}
