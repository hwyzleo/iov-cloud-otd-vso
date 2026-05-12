package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import net.hwyz.iov.cloud.dms.org.api.contract.DealershipExService;
//import net.hwyz.iov.cloud.dms.org.api.feign.service.ExDealershipService;
import net.hwyz.iov.cloud.framework.audit.annotation.Log;
import net.hwyz.iov.cloud.framework.audit.enums.BusinessType;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.MptAccount;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.security.annotation.RequiresPermissions;
import net.hwyz.iov.cloud.framework.security.util.SecurityUtils;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DeleteOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.PhysicalDeleteResponseVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.ApplyTransportRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.DeliveryCenterStaffMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.DeleteOrderRequestVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.PhysicalDeleteResponseVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.TransportOrderMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.VehicleSaleOrderMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AuditOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CancelOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.LockOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignDeliveryPersonCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ApplyTransportCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 管理后台订单接口
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequestMapping("/api/mpt/order/v1")
@RequiredArgsConstructor
public class MptOrderController extends BaseController {

    private final OrderAppService vehicleSaleOrderAppService;
//    private final ExDealershipService exDealershipService;

    /**
     * 分页查询车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:info:list")
    @GetMapping(value = "/list")
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> list(VehicleSaleOrderMpt vehicleSaleOrder) {
        log.info("管理后台用户[{}]分页查询车辆销售订单信息", SecurityUtils.getUsername());
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo)));
    }

    /**
     * 根据车辆销售订单ID获取车辆销售订单信息
     *
     * @param vehicleSaleOrderId 销售车型ID
     * @return 车辆销售订单信息
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{vehicleSaleOrderId}")
    public ApiResponse<VehicleSaleOrderMpt> getInfo(@PathVariable Long vehicleSaleOrderId) {
        log.info("管理后台用户[{}]根据车辆销售订单ID[{}]获取车辆销售订单信息", SecurityUtils.getUsername(), vehicleSaleOrderId);
        OrderDetailResult result = vehicleSaleOrderAppService.getById(vehicleSaleOrderId.toString());
        return ApiResponse.ok(VehicleSaleOrderMptAssembler.INSTANCE.toVo(result));
    }

    /**
     * 分页查询可改配车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:changeModel:list")
    @GetMapping(value = "/listModelConfigChangeable")
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> listModelConfigChangeable(VehicleSaleOrderMpt vehicleSaleOrder) {
        log.info("管理后台用户[{}]分页查询可改配车辆销售订单信息", SecurityUtils.getUsername());
        List<Integer> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.EARNEST_MONEY_PAID.getValue());
        orderStateRange.add(OrderState.DOWN_PAYMENT_PAID.getValue());
        orderStateRange.add(OrderState.ARRANGE_PRODUCTION.getValue());
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .orderStateRange(orderStateRange)
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo)));
    }

    /**
     * 分页查询没有交付人员的车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:list")
    @GetMapping(value = "/listWithoutDeliveryPerson")
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> listWithoutDeliveryPerson(VehicleSaleOrderMpt vehicleSaleOrder) {
        log.info("管理后台用户[{}]分页查询没有交付人员的车辆销售订单信息", SecurityUtils.getUsername());
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .hasDeliveryPerson(false)
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        List<VehicleSaleOrderMpt> voList = PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo);
        for (VehicleSaleOrderMpt vo : voList) {
            if (StrUtil.isNotBlank(vo.getDeliveryCenter())) {
//                DealershipExService dealership = exDealershipService.getByCode(vo.getDeliveryCenter());
//                if (ObjUtil.isNotNull(dealership)) {
//                    vo.setDeliveryCenterName(dealership.getName());
//                }
            }
        }
        return ApiResponse.ok(getPageResult(voList));
    }

    /**
     * 分页查询交付中心人员
     *
     * @param deliveryCenterStaff 交付中心人员信息
     * @return 交付中心人员信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:list")
    @GetMapping(value = "/listDeliveryCenterStaff")
    public ApiResponse<PageResult<DeliveryCenterStaffMpt>> listDeliveryCenterStaff(DeliveryCenterStaffMpt deliveryCenterStaff) {
        log.info("管理后台用户[{}]分页查询交付中心[{}]人员", SecurityUtils.getUsername(), deliveryCenterStaff.getDealershipCode());
        startPage();
        List<DeliveryStaffResult> result = vehicleSaleOrderAppService.searchDeliveryStaff(deliveryCenterStaff.getDealershipCode());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, DeliveryCenterStaffMptAssembler.INSTANCE::toVo)));
    }

    /**
     * 分页查询可配车车辆销售订单信息
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignVehicle:list")
    @GetMapping(value = "/listAssignable")
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> listAssignable(VehicleSaleOrderMpt vehicleSaleOrder) {
        log.info("管理后台用户[{}]分页查询可配车车辆销售订单信息", SecurityUtils.getUsername());
        List<Integer> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.ARRANGE_PRODUCTION.getValue());
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .orderStateRange(orderStateRange)
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo)));
    }

    /**
     * 分页查询运输相关车辆销售订单信息
     *
     * @param transportOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:transport:list")
    @GetMapping(value = "/listTransport")
    public ApiResponse<PageResult<TransportOrderMpt>> listTransport(TransportOrderMpt transportOrder) {
        log.info("管理后台用户[{}]分页查询运输相关车辆销售订单信息", SecurityUtils.getUsername());
        List<Integer> orderStateRange = new ArrayList<>();
        orderStateRange.add(OrderState.ALLOCATION_VEHICLE.getValue());
        orderStateRange.add(OrderState.APPLY_TRANSPORT.getValue());
        orderStateRange.add(OrderState.PREPARE_TRANSPORT.getValue());
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(transportOrder.getOrderNo())
                .orderState(transportOrder.getOrderState())
                .orderStateRange(orderStateRange)
                .beginTime(getBeginTime(transportOrder))
                .endTime(getEndTime(transportOrder))
                .build());
        List<TransportOrderMpt> voList = PageUtil.convert(result, TransportOrderMptAssembler.INSTANCE::toVo);
        for (TransportOrderMpt vo : voList) {
            if (StrUtil.isNotBlank(vo.getDeliveryCenter())) {
//                DealershipExService dealership = exDealershipService.getByCode(vo.getDeliveryCenter());
//                if (ObjUtil.isNotNull(dealership)) {
//                    vo.setDeliveryCenterName(dealership.getName());
//                }
            }
        }
        return ApiResponse.ok(getPageResult(voList));
    }

    /**
     * 分配交付人员
     *
     * @param request 分配交付人员请求
     */
    @PostMapping("/action/assignDeliveryPerson")
    public ApiResponse<Void> assignDeliveryPerson(@RequestBody @Valid AssignDeliveryPersonRequest request, @RequestHeader(required = false) MptAccount mptAccount) {
        log.info("管理后台用户[{}]分配交付人员", ParamHelper.getMptAccountInfo(mptAccount));
        vehicleSaleOrderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(request.getOrderNo())
                .deliveryPersonId(request.getDeliveryPersonId())
                .deliveryPersonName(request.getDeliveryPersonName())
                .build());
        return ApiResponse.ok();
    }

    /**
     * 分配车辆
     *
     * @param request 分配车辆请求
     */
    @PostMapping("/action/assignVehicle")
    public ApiResponse<Void> assignVehicle(@RequestBody @Valid AssignVehicleRequest request) {
        log.info("管理后台用户[{}]分配车辆[{}]到订单[{}]", SecurityUtils.getUsername(), request.getVin(), request.getOrderNo());
        vehicleSaleOrderAppService.assignVehicle(AssignVehicleCmd.builder()
                .orderNo(request.getOrderNo())
                .vin(request.getVin())
                .build());
        return ApiResponse.ok();
    }

    /**
     * 申请发运
     *
     * @param request 申请发运请求
     */
    @PostMapping("/action/applyTransport")
    public ApiResponse<Void> applyTransport(@RequestBody @Valid ApplyTransportRequest request) {
        log.info("管理后台用户[{}]申请发运订单[{}]", SecurityUtils.getUsername(), request.getOrderNo());
        vehicleSaleOrderAppService.applyTransport(ApplyTransportCmd.builder()
                .orderNo(request.getOrderNo())
                .build());
        return ApiResponse.ok();
    }

    @Log(title = "车辆销售订单管理", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:order:info:remove")
    @DeleteMapping("/{orderNo}")
    public ApiResponse<Integer> remove(@PathVariable String orderNo) {
        log.info("管理后台用户[{}]删除订单[{}]", SecurityUtils.getUsername(), orderNo);
        return ApiResponse.ok(vehicleSaleOrderAppService.remove(orderNo) ? 1 : 0);
    }

    /**
     * 物理删除订单及其所有关联数据
     *
     * @param orderId 订单业务 ID
     * @param request 删除请求
     * @return 删除结果
     */
    @Log(title = "订单物理删除", businessType = BusinessType.DELETE)
    @RequiresPermissions("completeVehicle:order:info:physicalDelete")
    @DeleteMapping("/physical/{orderId}")
    public ApiResponse<PhysicalDeleteResponseVo> physicalDelete(
            @PathVariable String orderId,
            @RequestBody DeleteOrderRequestVo request) {
        log.info("管理后台用户[{}]物理删除订单[{}]", SecurityUtils.getUsername(), orderId);

        DeleteOrderCmd cmd = DeleteOrderRequestVoAssembler.INSTANCE.toCmd(request);
        cmd.setOrderId(orderId);
        cmd.setOperatorId(SecurityUtils.getUsername());

        PhysicalDeleteResult result = vehicleSaleOrderAppService.deleteOrder(cmd);

        return ApiResponse.ok(PhysicalDeleteResponseVoAssembler.INSTANCE.toVo(result));
    }

    /**
     * 订单审核通过
     */
    @PostMapping("/{orderId}/audit/pass")
    public ApiResponse<Void> auditPass(@PathVariable String orderId,
                                         @RequestHeader("X-Operator-Id") String operatorId) {
        log.info("管理后台审核通过：orderId={}, operatorId={}", orderId, operatorId);
        
        try {
            vehicleSaleOrderAppService.auditPass(AuditOrderCmd.builder()
                    .orderId(orderId)
                    .operatorId(operatorId)
                    .build());
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("审核通过异常", e);
            return ApiResponse.fail("操作失败");
        }
    }

    /**
     * 订单审核驳回
     */
    @PostMapping("/{orderId}/audit/reject")
    public ApiResponse<Void> auditReject(@PathVariable String orderId,
                                           @RequestParam String reason,
                                           @RequestHeader("X-Operator-Id") String operatorId) {
        log.info("管理后台审核驳回：orderId={}, operatorId={}, reason={}", orderId, operatorId, reason);
        
        try {
            vehicleSaleOrderAppService.auditReject(AuditOrderCmd.builder()
                    .orderId(orderId)
                    .operatorId(operatorId)
                    .rejectReason(reason)
                    .build());
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("审核驳回异常", e);
            return ApiResponse.fail("操作失败");
        }
    }

    /**
     * 锁单
     */
    @PostMapping("/{orderId}/lock")
    public ApiResponse<Void> lockOrder(@PathVariable String orderId,
                                         @RequestHeader("X-Operator-Id") String operatorId) {
        log.info("管理后台锁单：orderId={}, operatorId={}", orderId, operatorId);
        
        try {
            vehicleSaleOrderAppService.lockOrder(LockOrderCmd.builder()
                    .orderId(orderId)
                    .operatorId(operatorId)
                    .build());
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("锁单异常", e);
            return ApiResponse.fail("操作失败");
        }
    }

    /**
     * 关闭订单
     */
    @PostMapping("/{orderId}/close")
    public ApiResponse<Void> closeOrder(@PathVariable String orderId,
                                          @RequestParam String reason,
                                          @RequestHeader("X-Operator-Id") String operatorId) {
        log.info("管理后台关闭订单：orderId={}, operatorId={}, reason={}", orderId, operatorId, reason);
        
        try {
            vehicleSaleOrderAppService.cancelOrder(CancelOrderCmd.builder()
                    .orderId(orderId)
                    .operatorId(operatorId)
                    .reason(reason)
                    .operateType("CLOSE")
                    .build());
            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("关闭订单异常", e);
            return ApiResponse.fail("操作失败");
        }
    }

    /**
     * 查询订单列表
     */
    @GetMapping
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> listOrders(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "20") Integer size) {
        log.info("管理后台查询订单列表：status={}, page={}, size={}", status, page, size);
        
        try {
            // TODO: 实现订单列表查询
            return ApiResponse.ok(new PageResult<>());
        } catch (Exception e) {
            log.error("查询订单列表异常", e);
            return ApiResponse.fail("查询失败");
        }
    }

    private Date getBeginTime(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) ((Map) obj).get("params");
            if (params != null && params.get("beginTime") != null) {
                return (Date) params.get("beginTime");
            }
        }
        return null;
    }

    private Date getEndTime(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> params = (Map<String, Object>) ((Map) obj).get("params");
            if (params != null && params.get("endTime") != null) {
                return (Date) params.get("endTime");
            }
        }
        return null;
    }

}
