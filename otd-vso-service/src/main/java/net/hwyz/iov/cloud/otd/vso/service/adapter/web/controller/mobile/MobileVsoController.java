package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import jakarta.validation.Valid;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.*;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.*;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ResubmitAuditCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.WishlistAppService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/mobile/vso/v1")
public class MobileVsoController extends BaseController {

    private final OrderAppService vehicleSaleOrderAppService;
    private final WishlistAppService wishlistAppService;

    /**
     * 获取我的车辆列表（合并心愿单和订单）
     */
    @GetMapping("/myVehicleList")
    public ApiResponse<List<MyVehicleVo>> getMyVehicleList() {
        log.info("手机客户端[{}]获取我的车辆列表", ParamHelper.getClientAccountInfo());
        String accountId = SecurityContextHolder.getUserId();

        List<WishlistListResult> wishlists = wishlistAppService.getWishlistList(accountId);
        List<OrderListResult> orders = vehicleSaleOrderAppService.search(OrderQuery.builder().type("valid").build());

        List<MyVehicleVo> myVehicles = new ArrayList<>();
        
        wishlists.sort(Comparator.comparing(WishlistListResult::getModifyTime, Comparator.nullsLast(Comparator.reverseOrder())));
        myVehicles.addAll(MyVehicleAssembler.INSTANCE.fromWishlistList(wishlists));
        
        orders.sort(Comparator.comparing(OrderListResult::getModifyTime, Comparator.nullsLast(Comparator.reverseOrder())));
        myVehicles.addAll(MyVehicleAssembler.INSTANCE.fromOrderList(orders));

        return ApiResponse.ok(myVehicles);
    }

    /**
     * 获取订单列表
     */
    @GetMapping("/order")
    public ApiResponse<List<OrderVo>> getOrderList(@RequestParam(required = false) String type) {
        log.info("手机客户端[{}]获取[{}]订单列表", ParamHelper.getClientAccountInfo(), type);
        OrderQuery query = OrderQuery.builder()
                .type(type)
                .build();
        List<OrderListResult> result = vehicleSaleOrderAppService.search(query);
        return ApiResponse.ok(OrderVoAssembler.INSTANCE.toVoList(result));
    }

    /**
     * 创建心愿单
     */
    @PostMapping("/wishlist/action/create")
    public ApiResponse<String> createWishlist(@RequestBody @Valid CreateWishlistRequestVo request) {
        log.info("手机客户端[{}]创建心愿单", ParamHelper.getClientAccountInfo());
        CreateWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toCreateWishlistCmd(SecurityContextHolder.getUserId(), request);
        String wishlistId = wishlistAppService.createWishlist(cmd);
        return ApiResponse.ok(wishlistId);
    }

    /**
     * 修改心愿单
     */
    @PostMapping("/wishlist/action/modify")
    public ApiResponse<Void> modifyWishlist(@RequestBody @Valid ModifyWishlistRequestVo request) {
        log.info("手机客户端[{}]修改心愿单[{}]", ParamHelper.getClientAccountInfo(), request.getWishlistId());
        ModifyWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toModifyWishlistCmd(SecurityContextHolder.getUserId(), request);
        wishlistAppService.modifyWishlist(cmd);
        return ApiResponse.ok();
    }

    /**
     * 删除心愿单
     */
    @PostMapping("/wishlist/action/delete")
    public ApiResponse<Void> deleteWishlist(@RequestBody @Valid DeleteWishlistRequestVo request) {
        log.info("手机客户端[{}]删除心愿单[{}]", ParamHelper.getClientAccountInfo(), request.getWishlistId());
        DeleteWishlistCmd cmd = WishlistVoAssembler.INSTANCE.toDeleteWishlistCmd(SecurityContextHolder.getUserId(), request);
        wishlistAppService.deleteWishlist(cmd);
        return ApiResponse.ok();
    }

    /**
     * 获取心愿单详情
     */
    @GetMapping("/wishlist/{wishlistId}")
    public ApiResponse<WishlistDetailVo> getWishlist(@PathVariable String wishlistId) {
        log.info("手机客户端[{}]获取心愿单[{}]详情", ParamHelper.getClientAccountInfo(), wishlistId);
        WishlistDetailResult result = wishlistAppService.getWishlistDetail(SecurityContextHolder.getUserId(), wishlistId);
        WishlistDetailVo vo = WishlistVoAssembler.INSTANCE.toDetailVo(result);
        return ApiResponse.ok(vo);
    }

    /**
     * 意向金下订单
     */
    @PostMapping("/action/earnestMoneyOrder")
    public ApiResponse<EarnestMoneyOrderResult> earnestMoneyOrder(@RequestBody @Valid EarnestMoneyOrderRequestVo request) {
        log.info("手机客户端[{}]意向金下订单", ParamHelper.getClientAccountInfo());
        EarnestMoneyCmd cmd = EarnestMoneyOrderRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        EarnestMoneyOrderResult result = vehicleSaleOrderAppService.earnestMoneyOrder(cmd);
        return ApiResponse.ok(result);
    }

    /**
     * 发起支付
     */
    @PostMapping("/action/initiatePayment")
    public ApiResponse<InitiatePaymentResult> initiatePayment(@RequestBody @Valid InitiatePaymentRequestVo request) {
        log.info("手机客户端[{}]发起支付：orderNo={}, paymentChannel={}",
                ParamHelper.getClientAccountInfo(), request.getOrderNo(), request.getPaymentChannel());
        InitiatePaymentCmd cmd = InitiatePaymentCmd.builder()
                .accountId(SecurityContextHolder.getUserId())
                .orderNo(request.getOrderNo())
                .paymentChannel(request.getPaymentChannel())
                .build();
        InitiatePaymentResult result = vehicleSaleOrderAppService.initiatePayment(cmd);
        return ApiResponse.ok(result);
    }

    /**
     * 定金下订单
     * <p>
     * 请求参数说明：
     * <ul>
     *     <li>saleModelCode - 销售车型代码，必填（意向金下单参数）</li>
     *     <li>orderNo - 订单号，可选（意向金下单参数，用于从意向金订单转定金）</li>
     *     <li>saleModelConfigType - 特征配置，必填（意向金下单参数）</li>
     *     <li>customerType - 客户类型，可选（意向金转定金参数）</li>
     *     <li>paymentMethod - 支付方式，可选（意向金转定金参数）</li>
     *     <li>orderPersonType - 订购人类型，可选（意向金转定金参数）</li>
     *     <li>orderPersonName - 订购人姓名，可选（意向金转定金参数）</li>
     *     <li>orderPersonIdType - 订购人证件类型，可选（意向金转定金参数）</li>
     *     <li>orderPersonIdNum - 订购人证件号码，可选（意向金转定金参数）</li>
     *     <li>purchasePlan - 购买计划，可选（意向金转定金参数）</li>
     *     <li>licenseCityCode - 上牌城市代码，可选（意向金下单+意向金转定金参数）</li>
     *     <li>orderStoreCode - 下单门店编码，可选（意向金转定金参数）</li>
     *     <li>deliveryStoreCode - 交付门店编码，可选（意向金转定金参数）</li>
     * </ul>
     */
    @PostMapping("/action/downPaymentOrder")
    public ApiResponse<DownPaymentOrderResult> downPaymentOrder(@RequestBody @Valid DownPaymentOrderRequestVo request) {
        log.info("手机客户端[{}]定金下订单", ParamHelper.getClientAccountInfo());
        DownPaymentCmd cmd = DownPaymentOrderRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        DownPaymentOrderResult result = vehicleSaleOrderAppService.downPaymentOrder(cmd);
        return ApiResponse.ok(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/order/{orderNo}")
    public ApiResponse<OrderResponseVo> getOrder(@PathVariable String orderNo) {
        log.info("手机客户端[{}]获取订单[{}]详情", ParamHelper.getClientAccountInfo(), orderNo);
        OrderDetailResult result = vehicleSaleOrderAppService.getUserOrder(SecurityContextHolder.getUserId(), orderNo);
        OrderResponseVo vo = OrderResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    /**
     * 取消订单
     * <p>
     * 请求参数说明：仅需 orderNo（订单号）
     */
    @PostMapping("/order/action/cancel")
    public ApiResponse<Void> cancel(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]取消订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        CancelCmd cmd = OrderVoAssembler.INSTANCE.toCancelCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.cancel(cmd);
        return ApiResponse.ok();
    }

    /**
     * 支付订单
     */
    @PostMapping("/order/action/pay")
    public ApiResponse<OrderPaymentResponseVo> pay(@RequestBody @Valid OrderPaymentRequestVo request) {
        log.info("手机客户端[{}]支付订单[{}]", ParamHelper.getClientAccountInfo(), request.getOrderNo());
        PayCmd cmd = OrderPaymentRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        PayResult result = vehicleSaleOrderAppService.pay(cmd);
        OrderPaymentResponseVo vo = OrderPaymentResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    /**
     * 退款订单
     * <p>
     * 请求参数说明：仅需 orderNo（订单号）
     */
    @PostMapping("/order/action/requestRefund")
    public ApiResponse<Void> requestRefund(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]退款订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        RequestRefundCmd cmd = OrderResponseVoAssembler.INSTANCE.toRequestRefundCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.requestRefund(cmd);
        return ApiResponse.ok();
    }

    /**
     * 意向金转定金
     * <p>
     * 请求参数说明：
     * <ul>
     *     <li>orderNo - 订单号，必填</li>
     *     <li>customerType - 客户类型，可选</li>
     *     <li>paymentMethod - 支付方式，可选</li>
     *     <li>orderPersonType - 订购人类型，可选</li>
     *     <li>orderPersonName - 订购人姓名，可选</li>
     *     <li>orderPersonIdType - 订购人证件类型，可选</li>
     *     <li>orderPersonIdNum - 订购人证件号码，可选</li>
     *     <li>purchasePlan - 购买计划，可选</li>
     *     <li>licenseCityCode - 上牌城市代码，可选</li>
     *     <li>orderStoreCode - 下单门店编码，可选</li>
     *     <li>deliveryStoreCode - 交付门店编码，可选</li>
     * </ul>
     *
     * @return 差额>0时返回supplementaryPayment信息，差额<=0时直接完成转换
     */
    @PostMapping("/order/action/earnestMoneyToDownPayment")
    public ApiResponse<EarnestToDownResponseVo> earnestMoneyToDownPayment(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]订单[{}]意向金转定金", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        EarnestToDownCmd cmd = OrderVoAssembler.INSTANCE.toEarnestToDownCmd(SecurityContextHolder.getUserId(), order);
        EarnestToDownResult result = vehicleSaleOrderAppService.earnestMoneyToDownPayment(cmd);

        EarnestToDownResponseVo response = EarnestToDownResponseVo.builder()
            .orderNo(result.getOrderNo())
            .orderType(result.getOrderType().name())
            .orderState(result.getOrderState().getValue().toString())
            .build();

        if (result.getSupplementaryPayment() != null) {
            response.setSupplementaryPayment(EarnestToDownResponseVo.SupplementaryPaymentVo.builder()
                .supplementaryNo(result.getSupplementaryPayment().getSupplementaryNo())
                .amount(result.getSupplementaryPayment().getAmount().getAmount())
                .currency(result.getSupplementaryPayment().getAmount().getCurrency())
                .expireTime(result.getSupplementaryPayment().getExpireTime())
                .build());
        }

        return ApiResponse.ok(response);
    }

    /**
     * 锁定订单
     * <p>
     * 请求参数说明：仅需 orderNo（订单号）
     */
    @PostMapping("/order/action/lock")
    public ApiResponse<Void> lock(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]锁定订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        LockCmd cmd = OrderVoAssembler.INSTANCE.toLockCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.lock(cmd);
        return ApiResponse.ok();
    }

    /**
     * 修改订单配置
     */
    @PostMapping("/order/action/modifyConfig")
    public ApiResponse<Void> modifyConfig(@RequestBody @Valid ModifyOrderConfigRequestVo request) {
        log.info("手机客户端[{}]修改订单[{}]配置", ParamHelper.getClientAccountInfo(), request.getOrderNo());
        ModifyOrderConfigCmd cmd = ModifyOrderConfigRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        vehicleSaleOrderAppService.modifyConfig(cmd);
        return ApiResponse.ok();
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<?> getOrderDetail(@PathVariable String orderId) {
        log.info("C 端查询订单详情：orderId={}", orderId);

        try {
            return ApiResponse.ok(vehicleSaleOrderAppService.getById(orderId));
        } catch (Exception e) {
            log.error("查询订单详情异常", e);
            return ApiResponse.fail("系统繁忙，请稍后再试");
        }
    }

    /**
     * 取消订单
     */
    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancelOrder(@PathVariable String orderId,
                                         @RequestParam String reason) {
        log.info("C 端取消订单：orderId={}, reason={}", orderId, reason);

        try {
            String operatorId = "CAPP_USER";
            vehicleSaleOrderAppService.cancelOrder(CancelOrderCmd.builder()
                    .orderId(orderId)
                    .operatorId(operatorId)
                    .reason(reason)
                    .operateType("CANCEL")
                    .build());
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            log.warn("取消订单失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("取消订单异常", e);
            return ApiResponse.fail("系统繁忙，请稍后再试");
        }
    }

    /**
     * 重提审核
     */
    @PostMapping("/order/action/resubmitAudit")
    public ApiResponse<Void> resubmitAudit(@RequestBody OrderVo order) {
        log.info("C 端重提审核：orderNo={}", order.getOrderNo());

        try {
            String operatorId = "CAPP_USER";
            vehicleSaleOrderAppService.resubmitAudit(ResubmitAuditCmd.builder()
                    .orderId(order.getOrderNo())
                    .operatorId(operatorId)
                    .build());
            return ApiResponse.ok();
        } catch (IllegalArgumentException e) {
            log.warn("重提审核失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("重提审核异常", e);
            return ApiResponse.fail("系统繁忙，请稍后再试");
        }
    }

}
