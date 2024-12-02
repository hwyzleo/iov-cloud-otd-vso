package net.hwyz.iov.cloud.otd.vso.service.facade.mpt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.otd.vso.api.contract.SaleModelMpt;
import net.hwyz.iov.cloud.otd.vso.api.feign.mpt.SaleModelMptApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelAppService;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.SaleModelMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 销售车型相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/saleModel")
public class SaleModelMptController extends BaseController implements SaleModelMptApi {

    private final SaleModelAppService saleModelAppService;

    /**
     * 分页查询销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 销售车型信息列表
     */
    @RequiresPermissions("otd:saleModel:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(SaleModelMpt saleModel) {
        logger.info("管理后台用户[{}]分页查询销售车型信息", SecurityUtils.getUsername());
        startPage();
        List<SaleModelPo> saleModelPoList = saleModelAppService.search(saleModel.getSaleCode(), saleModel.getModelName(),
                getBeginTime(saleModel), getEndTime(saleModel));
        List<SaleModelMpt> saleModelMptList = SaleModelMptAssembler.INSTANCE.fromPoList(saleModelPoList);
        return getDataTable(saleModelMptList);
    }

    /**
     * 导出销售车型信息
     *
     * @param response  响应
     * @param saleModel 销售车型信息
     */
    @Log(title = "销售车型管理", businessType = BusinessType.EXPORT)
    @RequiresPermissions("otd:saleModel:export")
    @Override
    @PostMapping("/export")
    public void export(HttpServletResponse response, SaleModelMpt saleModel) {
        logger.info("管理后台用户[{}]导出销售车型信息", SecurityUtils.getUsername());
    }

    /**
     * 根据销售车型ID获取销售车型信息
     *
     * @param saleModelId 销售车型ID
     * @return 销售车型信息
     */
    @RequiresPermissions("otd:saleModel:query")
    @Override
    @GetMapping(value = "/{saleModelId}")
    public AjaxResult getInfo(@PathVariable Long saleModelId) {
        logger.info("管理后台用户[{}]根据销售车型ID[{}]获取销售车型信息", SecurityUtils.getUsername(), saleModelId);
        SaleModelPo saleModelPo = saleModelAppService.getSaleModelById(saleModelId);
        return success(SaleModelMptAssembler.INSTANCE.fromPo(saleModelPo));
    }

    /**
     * 新增销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    @Log(title = "销售车型管理", businessType = BusinessType.INSERT)
    @RequiresPermissions("otd:saleModel:add")
    @Override
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SaleModelMpt saleModel) {
        logger.info("管理后台用户[{}]新增销售车型信息[{}]", SecurityUtils.getUsername(), saleModel.getSaleCode());
        if (!saleModelAppService.checkSaleCodeUnique(saleModel.getId(), saleModel.getSaleCode())) {
            return error("新增销售车型'" + saleModel.getSaleCode() + "'失败，销售编码已存在");
        }
        SaleModelPo saleModelPo = SaleModelMptAssembler.INSTANCE.toPo(saleModel);
        saleModelPo.setCreateBy(SecurityUtils.getUserId().toString());
        return toAjax(saleModelAppService.createSaleModel(saleModelPo));
    }

    /**
     * 修改保存销售车型信息
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    @Log(title = "销售车型管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("otd:saleModel:edit")
    @Override
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SaleModelMpt saleModel) {
        logger.info("管理后台用户[{}]修改保存销售车型信息[{}]", SecurityUtils.getUsername(), saleModel.getSaleCode());
        if (!saleModelAppService.checkSaleCodeUnique(saleModel.getId(), saleModel.getSaleCode())) {
            return error("修改保存销售车型'" + saleModel.getSaleCode() + "'失败，销售编码已存在");
        }
        SaleModelPo saleModelPo = SaleModelMptAssembler.INSTANCE.toPo(saleModel);
        saleModelPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(saleModelAppService.modifySaleModel(saleModelPo));
    }

    /**
     * 修改保存销售车型图片集
     *
     * @param saleModel 销售车型信息
     * @return 结果
     */
    @Log(title = "销售车型管理", businessType = BusinessType.UPDATE)
    @RequiresPermissions("otd:saleModel:edit")
    @Override
    @PutMapping("/images")
    public AjaxResult editImages(@Validated @RequestBody SaleModelMpt saleModel) {
        logger.info("管理后台用户[{}]修改保存销售车型图片集[{}]", SecurityUtils.getUsername(), saleModel.getSaleCode());
        if (!saleModelAppService.checkSaleCodeUnique(saleModel.getId(), saleModel.getSaleCode())) {
            return error("修改保存销售车型'" + saleModel.getSaleCode() + "'失败，销售编码已存在");
        }
        SaleModelPo saleModelPo = SaleModelMptAssembler.INSTANCE.toPo(saleModel);
        saleModelPo.setModifyBy(SecurityUtils.getUserId().toString());
        return toAjax(saleModelAppService.modifySaleModelImages(saleModelPo));
    }

    /**
     * 删除销售车型信息
     *
     * @param saleModelIds 销售车型ID数组
     * @return 结果
     */
    @Log(title = "销售车型管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("otd:saleModel:remove")
    @Override
    @DeleteMapping("/{saleModelIds}")
    public AjaxResult remove(@PathVariable Long[] saleModelIds) {
        logger.info("管理后台用户[{}]删除销售车型信息[{}]", SecurityUtils.getUsername(), saleModelIds);
        return toAjax(saleModelAppService.deleteSaleModelByIds(saleModelIds));
    }
}