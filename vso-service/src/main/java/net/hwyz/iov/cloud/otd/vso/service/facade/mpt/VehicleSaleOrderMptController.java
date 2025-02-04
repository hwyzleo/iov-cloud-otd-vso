package net.hwyz.iov.cloud.otd.vso.service.facade.mpt;

import cn.hutool.core.util.ObjUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.dms.org.api.contract.DealershipExService;
import net.hwyz.iov.cloud.dms.org.api.contract.DealershipStaffExService;
import net.hwyz.iov.cloud.dms.org.api.feign.service.ExDealershipService;
import net.hwyz.iov.cloud.dms.org.api.feign.service.ExDealershipStaffService;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.bean.MptAccount;
import net.hwyz.iov.cloud.framework.common.bean.Page;
import net.hwyz.iov.cloud.framework.common.util.Convert;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.common.util.ServletUtil;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.framework.common.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.common.web.domain.AjaxResult;
import net.hwyz.iov.cloud.framework.common.web.page.TableDataInfo;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.otd.vso.api.contract.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.ApplyTransportRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.contract.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.api.feign.mpt.VehicleSaleOrderMptApi;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleSaleOrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.facade.assembler.TransportOrderMptAssembler;
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

    private final ExDealershipService exDealershipService;
    private final ExDealershipStaffService exDealershipStaffService;
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
                vehicleSaleOrder.getOrderState(), null, null, getBeginTime(vehicleSaleOrder),
                getEndTime(vehicleSaleOrder));
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
                vehicleSaleOrder.getOrderState(), orderStateRange, null, getBeginTime(vehicleSaleOrder),
                getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分页查询没有交付人员的车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:list")
    @Override
    @GetMapping(value = "/listWithoutDeliveryPerson")
    public TableDataInfo listWithoutDeliveryPerson(VehicleSaleOrderMpt vehicleSaleOrder) {
        logger.info("管理后台用户[{}]分页查询没有交付人员的车辆销售订单信息", SecurityUtils.getUsername());
        startPage();
        List<OrderPo> orderPoList = vehicleSaleOrderAppService.search(vehicleSaleOrder.getOrderNum(),
                vehicleSaleOrder.getOrderState(), null, false, getBeginTime(vehicleSaleOrder),
                getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        for (VehicleSaleOrderMpt vehicleSaleOrderMpt : vehicleSaleOrderMptList) {
            DealershipExService dealership = exDealershipService.getByCode(vehicleSaleOrderMpt.getDeliveryCenter());
            if (ObjUtil.isNotNull(dealership)) {
                vehicleSaleOrderMpt.setDeliveryCenterName(dealership.getName());
            }
        }
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分页查询交付中心人员
     *
     * @param deliveryCenterStaff 交付中心人员信息
     * @return 交付中心人员信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:list")
    @Override
    @GetMapping(value = "/listDeliveryCenterStaff")
    public TableDataInfo listDeliveryCenterStaff(DeliveryCenterStaffMpt deliveryCenterStaff) {
        logger.info("管理后台用户[{}]分页查询交付中心[{}]人员", SecurityUtils.getUsername(), deliveryCenterStaff.getDealershipCode());
        Page<DealershipStaffExService> dealershipStaffPage = exDealershipStaffService.searchPage(deliveryCenterStaff.getDealershipCode(),
                Convert.toInt(ServletUtil.getParameter("pageNum"), 1),
                Convert.toInt(ServletUtil.getParameter("pageSize"), 10));
        List<DeliveryCenterStaffMpt> deliveryCenterStaffList = dealershipStaffPage.getRows().stream().map(dealershipStaff -> DeliveryCenterStaffMpt.builder()
                .dealershipCode(dealershipStaff.getDealershipCode())
                .dealershipName(dealershipStaff.getDealershipName())
                .userId(dealershipStaff.getUserId())
                .userName(dealershipStaff.getUserName())
                .nickName(dealershipStaff.getNickName())
                .phonenumber(dealershipStaff.getPhonenumber())
                .notDeliveryOrderCount(vehicleSaleOrderAppService.count(dealershipStaff.getUserId().toString(), false))
                .build()).toList();
        return getDataTable(deliveryCenterStaffList, dealershipStaffPage.getTotal());
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
                vehicleSaleOrder.getOrderState(), orderStateRange, null, getBeginTime(vehicleSaleOrder),
                getEndTime(vehicleSaleOrder));
        List<VehicleSaleOrderMpt> vehicleSaleOrderMptList = VehicleSaleOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        return getDataTable(orderPoList, vehicleSaleOrderMptList);
    }

    /**
     * 分页查询运输相关车辆销售订单信息
     *
     * @param transportOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:transport:list")
    @Override
    @GetMapping(value = "/listTransport")
    public TableDataInfo listTransport(TransportOrderMpt transportOrder) {
        logger.info("管理后台用户[{}]分页查询运输相关车辆销售订单信息", SecurityUtils.getUsername());
        List<OrderState> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.ALLOCATION_VEHICLE);
        orderStateRange.add(OrderState.APPLY_TRANSPORT);
        orderStateRange.add(OrderState.PREPARE_TRANSPORT);
        startPage();
        List<OrderPo> orderPoList = vehicleSaleOrderAppService.search(transportOrder.getOrderNum(),
                transportOrder.getOrderState(), orderStateRange, null, getBeginTime(transportOrder),
                getEndTime(transportOrder));
        List<TransportOrderMpt> transportOrderMptList = TransportOrderMptAssembler.INSTANCE.fromPoList(orderPoList);
        for (TransportOrderMpt transportOrderMpt : transportOrderMptList) {
            if (StrUtil.isNotBlank(transportOrderMpt.getDeliveryCenter())) {
                DealershipExService dealership = exDealershipService.getByCode(transportOrderMpt.getDeliveryCenter());
                if (ObjUtil.isNotNull(dealership)) {
                    transportOrderMpt.setDeliveryCenterName(dealership.getName());
                }
            }
        }
        return getDataTable(orderPoList, transportOrderMptList);
    }

    /**
     * 分配交付人员
     *
     * @param request 分配交付人员请求
     */
    @Override
    @PostMapping("/action/assignDeliveryPerson")
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
    @PostMapping("/action/assignVehicle")
    public void assignVehicle(@RequestBody @Valid AssignVehicleRequest request) {
        logger.info("管理后台用户[{}]分配车辆[{}]到订单[{}]", SecurityUtils.getUsername(), request.getVin(), request.getOrderNum());
        vehicleSaleOrderAppService.assignVehicle(request.getOrderNum(), request);
    }

    /**
     * 申请发运
     *
     * @param request 申请发运请求
     */
    @Override
    @PostMapping("/action/applyTransport")
    public void applyTransport(@RequestBody @Valid ApplyTransportRequest request) {
        logger.info("管理后台用户[{}]申请发运订单[{}]", SecurityUtils.getUsername(), request.getOrderNum());
        vehicleSaleOrderAppService.applyTransport(request.getOrderNum(), SecurityUtils.getUserId().toString(), SecurityUtils.getUsername());
    }

    /**
     * 删除订单
     *
     * @param orderNum 订单号
     * @return 结果
     */
    @Log(title = "车辆销售订单管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:order:info:remove")
    @Override
    @DeleteMapping("/{orderNum}")
    public AjaxResult remove(@PathVariable String orderNum) {
        logger.info("管理后台用户[{}]删除订单[{}]", SecurityUtils.getUsername(), orderNum);
        return toAjax(vehicleSaleOrderAppService.remove(orderNum));
    }
}
