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
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.*;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/mobile/order/v1")
public class MobileOrderController extends BaseController {

    private final OrderAppService vehicleSaleOrderAppService;

    @GetMapping("/order")
    public ApiResponse<List<OrderVo>> getOrderList(@RequestParam(required = false) String type) {
        log.info("手机客户端[{}]获取[{}]订单列表", ParamHelper.getClientAccountInfo(), type);
        OrderQuery query = OrderQuery.builder()
                .type(type)
                .build();
        List<OrderListResult> result = vehicleSaleOrderAppService.search(query);
        return ApiResponse.ok(OrderVoAssembler.INSTANCE.toVoList(result));
    }

    @PostMapping("/wishlist/action/create")
    public ApiResponse<String> createWishlist(@RequestBody @Valid SelectedSaleModelRequestVo request) {
        log.info("手机客户端[{}]新建心愿单", ParamHelper.getClientAccountInfo());
        CreateWishlistCmd cmd = SelectedSaleModelRequestVoAssembler.INSTANCE.toCreateWishlistCmd(SecurityContextHolder.getUserId(), request);
        String orderNo = vehicleSaleOrderAppService.createUserWishlist(cmd);
        return ApiResponse.ok(orderNo);
    }

    @PostMapping("/wishlist/action/modify")
    public ApiResponse<Void> modifyWishlist(@RequestBody @Valid SelectedSaleModelRequestVo request) {
        log.info("手机客户端[{}]修改心愿单[{}]", ParamHelper.getClientAccountInfo(), request.getOrderNo());
        ModifyWishlistCmd cmd = SelectedSaleModelRequestVoAssembler.INSTANCE.toModifyWishlistCmd(SecurityContextHolder.getUserId(), request);
        vehicleSaleOrderAppService.modifyUserWishlist(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/wishlist/action/delete")
    public ApiResponse<Void> deleteWishlist(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]删除心愿单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        DeleteWishlistCmd cmd = OrderVoAssembler.INSTANCE.toDeleteWishlistCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.deleteUserWishlist(cmd);
        return ApiResponse.ok();
    }

    @GetMapping("/wishlist/{orderNo}")
    public ApiResponse<WishlistResponseVo> getWishlist(@PathVariable String orderNo) {
        log.info("手机客户端[{}]获取心愿单[{}]详情", ParamHelper.getClientAccountInfo(), orderNo);
        WishlistDetailResult result = vehicleSaleOrderAppService.getUserWishlist(SecurityContextHolder.getUserId(), orderNo);
        WishlistResponseVo vo = WishlistResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/action/earnestMoneyOrder")
    public ApiResponse<String> earnestMoneyOrder(@RequestBody @Valid EarnestMoneyOrderRequestVo request) {
        log.info("手机客户端[{}]意向金下订单", ParamHelper.getClientAccountInfo());
        EarnestMoneyCmd cmd = EarnestMoneyOrderRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        String orderNo = vehicleSaleOrderAppService.earnestMoneyOrder(cmd);
        return ApiResponse.ok(orderNo);
    }

    @PostMapping("/action/downPaymentOrder")
    public ApiResponse<String> downPaymentOrder(@RequestBody @Valid DownPaymentOrderRequestVo request) {
        log.info("手机客户端[{}]定金下订单", ParamHelper.getClientAccountInfo());
        DownPaymentCmd cmd = DownPaymentOrderRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        String orderNo = vehicleSaleOrderAppService.downPaymentOrder(cmd);
        return ApiResponse.ok(orderNo);
    }

    @GetMapping("/order/{orderNo}")
    public ApiResponse<OrderResponseVo> getOrder(@PathVariable String orderNo) {
        log.info("手机客户端[{}]获取订单[{}]详情", ParamHelper.getClientAccountInfo(), orderNo);
        OrderDetailResult result = vehicleSaleOrderAppService.getUserOrder(SecurityContextHolder.getUserId(), orderNo);
        OrderResponseVo vo = OrderResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/order/action/cancel")
    public ApiResponse<Void> cancel(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]取消订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        CancelCmd cmd = OrderVoAssembler.INSTANCE.toCancelCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.cancel(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/pay")
    public ApiResponse<OrderPaymentResponseVo> pay(@RequestBody @Valid OrderPaymentRequestVo request) {
        log.info("手机客户端[{}]支付订单[{}]", ParamHelper.getClientAccountInfo(), request.getOrderNo());
        PayCmd cmd = OrderPaymentRequestVoAssembler.INSTANCE.toCmd(SecurityContextHolder.getUserId(), request);
        PayResult result = vehicleSaleOrderAppService.pay(cmd);
        OrderPaymentResponseVo vo = OrderPaymentResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/order/action/requestRefund")
    public ApiResponse<Void> requestRefund(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]退款订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        RequestRefundCmd cmd = OrderResponseVoAssembler.INSTANCE.toRequestRefundCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.requestRefund(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/earnestMoneyToDownPayment")
    public ApiResponse<Void> earnestMoneyToDownPayment(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]订单[{}]意向金转定金", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        EarnestToDownCmd cmd = OrderVoAssembler.INSTANCE.toEarnestToDownCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.earnestMoneyToDownPayment(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/lock")
    public ApiResponse<Void> lock(@RequestBody @Valid OrderVo order) {
        log.info("手机客户端[{}]锁定订单[{}]", ParamHelper.getClientAccountInfo(), order.getOrderNo());
        LockCmd cmd = OrderVoAssembler.INSTANCE.toLockCmd(SecurityContextHolder.getUserId(), order);
        vehicleSaleOrderAppService.lock(cmd);
        return ApiResponse.ok();
    }

    /**
     * 创建小订单
     */
    @PostMapping("/small")
    public ApiResponse<OrderCreateResult> createSmallOrder(@RequestBody @Valid CreateSmallOrderRequest request) {
        log.info("C 端创建小订单：userId={}", request.getUserId());

        try {
            OrderCreateResult result = vehicleSaleOrderAppService.createSmallOrder(CreateSmallOrderCmd.builder()
                    .orderSource(request.getOrderSource())
                    .userId(request.getUserId())
                    .name(request.getName())
                    .mobileHash(request.getMobileHash())
                    .idNoHash(request.getIdNoHash())
                    .modelCode(request.getModelCode())
                    .modelName(request.getModelName())
                    .configCode(request.getConfigCode())
                    .configName(request.getConfigName())
                    .colorCode(request.getColorCode())
                    .colorName(request.getColorName())
                    .build());

            return ApiResponse.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("创建小订单失败：{}", e.getMessage());
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("创建小订单异常", e);
            return ApiResponse.fail("系统繁忙，请稍后再试");
        }
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
            // TODO: 获取当前用户 ID
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateSmallOrderRequest {
        private String orderSource;
        private String userId;
        private String name;
        private String mobileHash;
        private String idNoHash;
        private String modelCode;
        private String modelName;
        private String configCode;
        private String configName;
        private String colorCode;
        private String colorName;
    }
}
