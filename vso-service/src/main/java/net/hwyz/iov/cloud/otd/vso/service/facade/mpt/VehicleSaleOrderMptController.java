package net.hwyz.iov.cloud.otd.vso.service.facade.mpt;

import cn.hutool.core.collection.ListUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.MptAccount;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.common.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.otd.vso.api.contract.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.api.feign.mpt.VehicleSaleOrderMptApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleSaleOrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.VehicleSaleOrderMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.OrderPo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 车辆销售订单相关管理接口实现类
 *
 * @author hwyz_leo
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/mpt/vehicleSaleOrder")
public class VehicleSaleOrderMptController extends BaseController implements VehicleSaleOrderMptApi {

    private final VehicleSaleOrderAppService vehicleSaleOrderAppService;

    /**
     * 分页查询车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:info:list")
    @Override
    @GetMapping(value = "/list")
    public TableDataInfo list(VehicleSaleOrderMpt vehicleSaleOrder) {
        logger.info("管理后台用户[{}]分页查询车辆销售订单信息", SecurityUtils.getUsername());
        startPage();
        List<OrderPo> orderPoList = vehicleSaleOrderAppService.search(vehicleSaleOrder.getOrderNum(),
                vehicleSaleOrder.getOrderState(), null, getBeginTime(vehicleSaleOrder), getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分页查询可改配车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:changeModel:list")
    @Override
    @GetMapping(value = "/listModelConfigChangeable")
    public TableDataInfo listModelConfigChangeable(VehicleSaleOrderMpt vehicleSaleOrder) {
        logger.info("管理后台用户[{}]分页查询可改配车辆销售订单信息", SecurityUtils.getUsername());
        List<OrderState> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.EARNEST_MONEY_PAID);
        orderStateRange.add(OrderState.DOWN_PAYMENT_PAID);
        orderStateRange.add(OrderState.ARRANGE_PRODUCTION);
        startPage();
        List<OrderPo> orderPoList = vehicleSaleOrderAppService.search(vehicleSaleOrder.getOrderNum(),
                vehicleSaleOrder.getOrderState(), orderStateRange, getBeginTime(vehicleSaleOrder), getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分页查询可配车车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignVehicle:list")
    @Override
    @GetMapping(value = "/listAssignable")
    public TableDataInfo listAssignable(VehicleSaleOrderMpt vehicleSaleOrder) {
        logger.info("管理后台用户[{}]分页查询可配车车辆销售订单信息", SecurityUtils.getUsername());
        List<OrderState> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.ARRANGE_PRODUCTION);
        startPage();
        List<OrderPo> orderPoList = vehicleSaleOrderAppService.search(vehicleSaleOrder.getOrderNum(),
                vehicleSaleOrder.getOrderState(), orderStateRange, getBeginTime(vehicleSaleOrder), getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分配交付人员
     *
     * @param request 分配交付人员请求
     */
    @Override
    @PostMapping("/order/action/assignDeliveryPerson")
    public void assignDeliveryPerson(@RequestBody @Valid AssignDeliveryPersonRequest request, @RequestHeader(required = false) MptAccount mptAccount) {
        logger.info("管理后台用户[{}]分配交付人员", ParamHelper.getMptAccountInfo(mptAccount));
        vehicleSaleOrderAppService.assignDeliveryPerson(request.getOrderNum(), request);
    }

    /**
     * 分配车辆
     *
     * @param request 分配车辆请求
     */
    @Override
    @PostMapping("/order/action/assignVehicle")
    public void assignVehicle(@RequestBody @Valid AssignVehicleRequest request) {
        logger.info("管理后台用户[{}]分配车辆[{}]到订单[{}]", SecurityUtils.getUsername(), request.getVin(), request.getOrderNum());
        vehicleSaleOrderAppService.assignVehicle(request.getOrderNum(), request);
    }

}
