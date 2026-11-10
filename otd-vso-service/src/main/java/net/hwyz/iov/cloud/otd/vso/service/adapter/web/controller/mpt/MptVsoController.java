package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.vo.DealershipExService;
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
import net.hwyz.iov.cloud.otd.vso.api.vo.SupplementaryPaymentVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.DeliveryCenterStaffMpt;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.TransportOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleAssignmentVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.VehicleSaleOrderMpt;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.ApplyTransportRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.AssignDeliveryPersonRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.ModifyOrderConfigByOperatorRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.AssignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.ReassignVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.api.vo.mpt.request.UnbindVehicleRequest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.DeliveryCenterStaffMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.DeleteOrderRequestVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.PhysicalDeleteResponseVoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.TransportOrderMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.VehicleAssignmentAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.VehicleSaleOrderMptAssembler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.DeleteOrderRequestVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.PhysicalDeleteResponseVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ApplyTransportCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignDeliveryPersonCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AuditOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CancelOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.LockOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigByOperatorCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ReassignVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ResubmitAuditCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.UnbindVehicleCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryAppointmentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderTimelineResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PhysicalDeleteResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PurchaseAgreementResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.VehicleAssignmentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.DeliveryAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PurchaseAgreementAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.RefundAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.VehicleAssignmentAppService;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理后台订单接口
 * <p>
 * 写动作仅限审核、锁单、关闭、配车/换绑/解绑、发运申请与交付人员分配；
 * 只读查询（详情、时间线、支付、补款、退款、协议确认、交付预约）不做 C 端账号隔离，
 * 但必须使用 MPT 权限点，且不得触发网关调用、状态推进或时间线追加。
 * <p>
 * MPT 订单域路径参数统一为业务 {@code orderId}（{@code vso_order.order_id}），
 * {@code VehicleSaleOrderMpt.id}（数据库主键）仅用于展示排查，不得作为路由参数。
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequestMapping("/api/mpt/vso/v1")
@RequiredArgsConstructor
public class MptVsoController extends BaseController {

    private final OrderAppService vehicleSaleOrderAppService;
    private final OrgDealershipService orgDealershipService;
    private final VehicleAssignmentAppService vehicleAssignmentAppService;
    private final PaymentAppService paymentAppService;
    private final RefundAppService refundAppService;
    private final DeliveryAppService deliveryAppService;
    private final PurchaseAgreementAppService purchaseAgreementAppService;

    // ==================== 订单列表 ====================

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
        startPage();
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo)));
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
        startPage();
        // 可改配状态必须与改配校验共用同一事实（OrderState#configModifiableStateValues），避免列表与提交不一致
        List<Integer> orderStateRange = OrderState.configModifiableStateValues();
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
     * <p>
     * 响应批量带出订单当前有效交付预约（appointmentNo/appointmentTime/appointmentStatus），
     * 避免交付人员分配页按订单逐条查询（N+1）。
     *
     * @param vehicleSaleOrder 车辆销售订单信息
     * @return 车辆销售订单信息列表
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:list")
    @GetMapping(value = "/listWithoutDeliveryPerson")
    public ApiResponse<PageResult<VehicleSaleOrderMpt>> listWithoutDeliveryPerson(VehicleSaleOrderMpt vehicleSaleOrder) {
        log.info("管理后台用户[{}]分页查询没有交付人员的车辆销售订单信息", SecurityUtils.getUsername());
        startPage();
        List<OrderListResult> result = vehicleSaleOrderAppService.search(OrderQuery.builder()
                .orderNo(vehicleSaleOrder.getOrderNo())
                .orderState(vehicleSaleOrder.getOrderState())
                .hasDeliveryPerson(false)
                .beginTime(getBeginTime(vehicleSaleOrder))
                .endTime(getEndTime(vehicleSaleOrder))
                .build());
        List<VehicleSaleOrderMpt> voList = PageUtil.convert(result, VehicleSaleOrderMptAssembler.INSTANCE::toVo);
        // 门店名称按 storeCode 去重后逐个获取，避免逐行远程调用（Feign 无批量接口）
        Map<String, String> storeNameMap = loadStoreNames(voList.stream()
                .map(VehicleSaleOrderMpt::getDeliveryStoreCode).collect(Collectors.toList()));
        voList.forEach(vo -> vo.setDeliveryStoreName(storeNameMap.get(vo.getDeliveryStoreCode())));
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
    public ApiResponse<PageResult<DeliveryCenterStaffMpt>> listDeliveryCenterStaff(
            DeliveryCenterStaffMpt deliveryCenterStaff,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("管理后台用户[{}]分页查询交付中心[{}]人员", SecurityUtils.getUsername(), deliveryCenterStaff.getDealershipCode());
        // 交付中心人员来自组织服务（远程分页），不走本地 PageHelper
        PageResult<DeliveryStaffResult> result = vehicleSaleOrderAppService.searchDeliveryStaff(
                deliveryCenterStaff.getDealershipCode(), pageNum, pageSize);
        return ApiResponse.ok(new PageResult<>(
                PageUtil.convert(result.getItems(), DeliveryCenterStaffMptAssembler.INSTANCE::toVo),
                result.getTotal(), result.getPage(), result.getSize()));
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
        startPage();
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
        startPage();
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
        Map<String, String> storeNameMap = loadStoreNames(voList.stream()
                .map(TransportOrderMpt::getDeliveryStoreCode).collect(Collectors.toList()));
        voList.forEach(vo -> vo.setDeliveryStoreName(storeNameMap.get(vo.getDeliveryStoreCode())));
        return ApiResponse.ok(getPageResult(voList));
    }

    // ==================== 订单详情与运营只读查询（US-011，设计 §5.4.2） ====================

    /**
     * 根据订单业务 ID 获取车辆销售订单详情
     *
     * @param orderId 订单业务 ID（非数据库主键）
     * @return 车辆销售订单信息
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}")
    public ApiResponse<VehicleSaleOrderMpt> getInfo(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]根据订单业务ID[{}]获取车辆销售订单信息", SecurityUtils.getUsername(), orderId);
        OrderDetailResult result = vehicleSaleOrderAppService.getById(orderId);
        return ApiResponse.ok(VehicleSaleOrderMptAssembler.INSTANCE.toVo(result));
    }

    /**
     * 查询订单时间线（含操作人、操作角色与操作来源）
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/timeline")
    public ApiResponse<List<OrderTimelineResult>> getOrderTimeline(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]时间线", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(vehicleSaleOrderAppService.getTimelineByOrderId(orderId));
    }

    /**
     * 查询订单支付记录（含支付阶段）
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/payments")
    public ApiResponse<List<PaymentResult>> listPayments(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]支付记录", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(paymentAppService.listPaymentsByOrderId(orderId));
    }

    /**
     * 查询订单补款任务
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/supplements")
    public ApiResponse<List<SupplementaryPaymentVo>> listSupplements(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]补款任务", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(vehicleSaleOrderAppService.getSupplementaryPaymentsByOrderId(orderId));
    }

    /**
     * 查询订单退款记录（含退款场景、退款金额与手续费，可按 §4.3 规则独立核对）
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/refunds")
    public ApiResponse<List<RefundResult>> listRefunds(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]退款记录", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(refundAppService.listByOrderId(orderId));
    }

    /**
     * 查询订单协议确认快照
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/agreements")
    public ApiResponse<List<PurchaseAgreementResult>> listAgreementConfirmations(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]协议确认快照", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(purchaseAgreementAppService.listConfirmations(orderId));
    }

    /**
     * 查询订单交付信息（当前有效预约 + 最近交付记录）
     * <p>
     * 尚未发生预约时返回 {@code null}，不合成预约单号。
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping(value = "/{orderId}/delivery")
    public ApiResponse<DeliveryAppointmentResult> getDelivery(@PathVariable("orderId") String orderId) {
        log.info("管理后台用户[{}]查询订单[{}]交付信息", SecurityUtils.getUsername(), orderId);
        return ApiResponse.ok(deliveryAppService.getByOrderId(orderId));
    }

    // ==================== 订单写操作 ====================

    /**
     * 分配交付人员
     *
     * @param request 分配交付人员请求
     */
    @RequiresPermissions("completeVehicle:order:assignDeliveryPerson:assign")
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
    @RequiresPermissions("completeVehicle:order:assignVehicle:assign")
    @PostMapping("/action/assignVehicle")
    public ApiResponse<Void> assignVehicle(@RequestBody @Valid AssignVehicleRequest request) {
        log.info("管理后台用户[{}]分配车辆[{}]到订单[{}]", SecurityUtils.getUsername(), request.getVin(), request.getOrderNo());
        vehicleSaleOrderAppService.assignVehicle(AssignVehicleCmd.builder()
                .orderNo(request.getOrderNo())
                .vin(request.getVin())
                .operatorId(SecurityUtils.getUsername())
                .build());
        return ApiResponse.ok();
    }

    /**
     * 申请发运
     *
     * @param request 申请发运请求
     */
    @RequiresPermissions("completeVehicle:order:transport:apply")
    @PostMapping("/action/applyTransport")
    public ApiResponse<Void> applyTransport(@RequestBody @Valid ApplyTransportRequest request) {
        log.info("管理后台用户[{}]申请发运订单[{}]", SecurityUtils.getUsername(), request.getOrderNo());
        vehicleSaleOrderAppService.applyTransport(ApplyTransportCmd.builder()
                .orderNo(request.getOrderNo())
                .operatorId(SecurityUtils.getUsername())
                .operatorName(SecurityUtils.getUsername())
                .build());
        return ApiResponse.ok();
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
    @RequiresPermissions("completeVehicle:order:info:audit")
    @PostMapping("/{orderId}/audit/pass")
    public ApiResponse<Void> auditPass(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台审核通过：orderId={}, operatorId={}", orderId, operatorId);
        vehicleSaleOrderAppService.auditPass(AuditOrderCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 订单审核驳回
     */
    @RequiresPermissions("completeVehicle:order:info:audit")
    @PostMapping("/{orderId}/audit/reject")
    public ApiResponse<Void> auditReject(@PathVariable String orderId,
                                         @RequestParam String rejectCategory,
                                         @RequestParam String rejectReason) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台审核驳回：orderId={}, operatorId={}, rejectCategory={}, reason={}",
                orderId, operatorId, rejectCategory, rejectReason);
        vehicleSaleOrderAppService.auditReject(AuditOrderCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .rejectCategory(rejectCategory)
                .rejectReason(rejectReason)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 订单审核重提
     */
    @RequiresPermissions("completeVehicle:order:info:audit")
    @PostMapping("/{orderId}/audit/resubmit")
    public ApiResponse<Void> resubmitAudit(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台重提审核：orderId={}, operatorId={}", orderId, operatorId);
        vehicleSaleOrderAppService.resubmitAudit(ResubmitAuditCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 运营改配提交（复用 C 端改配的配置解析、价格差额补/退与快照逻辑）
     * <p>
     * 仅允许 EARNEST_MONEY_PAID / DOWN_PAYMENT_UNPAID / DOWN_PAYMENT_PAID；
     * 差额为正创建补款任务，为负创建退款任务，时间线记录 operateSource=mpt。
     */
    @RequiresPermissions("completeVehicle:order:changeModel:edit")
    @PostMapping("/{orderId}/modifyConfig")
    public ApiResponse<Void> modifyConfig(@PathVariable("orderId") String orderId,
                                          @RequestBody @Valid ModifyOrderConfigByOperatorRequest request) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台用户[{}]提交订单[{}]改配：variantCode={}, optionCodes={}",
                operatorId, orderId, request.getVariantCode(), request.getOptionCodes());
        vehicleSaleOrderAppService.modifyConfigByOperator(ModifyOrderConfigByOperatorCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .variantCode(request.getVariantCode())
                .optionCodes(request.getOptionCodes())
                .reason(request.getReason())
                .build());
        return ApiResponse.ok();
    }

    /**
     * 锁单
     */
    @RequiresPermissions("completeVehicle:order:info:lock")
    @PostMapping("/{orderId}/lock")
    public ApiResponse<Void> lockOrder(@PathVariable String orderId) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台锁单：orderId={}, operatorId={}", orderId, operatorId);
        vehicleSaleOrderAppService.lockOrder(LockOrderCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 关闭订单
     */
    @RequiresPermissions("completeVehicle:order:info:close")
    @PostMapping("/{orderId}/close")
    public ApiResponse<Void> closeOrder(@PathVariable String orderId,
                                        @RequestParam String reason) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台关闭订单：orderId={}, operatorId={}, reason={}", orderId, operatorId, reason);
        vehicleSaleOrderAppService.cancelOrder(CancelOrderCmd.builder()
                .orderId(orderId)
                .operatorId(operatorId)
                .reason(reason)
                .operateType("CLOSE")
                .build());
        return ApiResponse.ok();
    }

    /**
     * 换绑VIN
     */
    @RequiresPermissions("completeVehicle:order:assignVehicle:reassign")
    @PostMapping("/action/reassignVehicle")
    public ApiResponse<Void> reassignVehicle(@RequestBody @Valid ReassignVehicleRequest request) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台换绑VIN：orderNo={}, newVin={}, operatorId={}", request.getOrderNo(), request.getNewVin(), operatorId);
        vehicleAssignmentAppService.reassignVehicle(ReassignVehicleCmd.builder()
                .orderNo(request.getOrderNo())
                .newVin(request.getNewVin())
                .operatorId(operatorId)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 解绑VIN
     */
    @RequiresPermissions("completeVehicle:order:assignVehicle:unbind")
    @PostMapping("/action/unbindVehicle")
    public ApiResponse<Void> unbindVehicle(@RequestBody @Valid UnbindVehicleRequest request) {
        String operatorId = SecurityUtils.getUsername();
        log.info("管理后台解绑VIN：orderNo={}, reason={}, operatorId={}",
                request.getOrderNo(), request.getUnbindReason(), operatorId);
        vehicleAssignmentAppService.unbindVehicle(UnbindVehicleCmd.builder()
                .orderNo(request.getOrderNo())
                .unbindReason(request.getUnbindReason())
                .operatorId(operatorId)
                .build());
        return ApiResponse.ok();
    }

    /**
     * 查询配车信息
     */
    @RequiresPermissions("completeVehicle:order:info:query")
    @GetMapping("/vehicleAssignment/{orderNo}")
    public ApiResponse<VehicleAssignmentVo> getVehicleAssignment(@PathVariable String orderNo) {
        log.info("管理后台查询配车信息：orderNo={}", orderNo);
        VehicleAssignmentResult result = vehicleAssignmentAppService.getVehicleAssignment(orderNo);
        if (result == null) {
            return ApiResponse.ok(null);
        }
        return ApiResponse.ok(VehicleAssignmentAssembler.INSTANCE.toVo(result));
    }

    /**
     * 按门店编码去重后获取门店名称（组织服务仅提供单条查询，去重后可将 N 次远程调用降为“不同门店数”次）
     */
    private Map<String, String> loadStoreNames(java.util.Collection<String> storeCodes) {
        if (storeCodes == null || storeCodes.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        Map<String, String> result = new java.util.HashMap<>();
        storeCodes.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .forEach(code -> {
                    try {
                        DealershipExService dealership = orgDealershipService.getByCode(code);
                        if (ObjUtil.isNotNull(dealership) && StrUtil.isNotBlank(dealership.getName())) {
                            result.put(code, dealership.getName());
                        }
                    } catch (Exception e) {
                        log.warn("获取门店名称失败：storeCode={}", code, e);
                    }
                });
        return result;
    }

}
