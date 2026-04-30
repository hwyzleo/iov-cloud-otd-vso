package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.bean.ClientAccount;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.framework.web.util.PageUtil;
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
    public ApiResponse<PageResult<OrderVo>> getOrderList(@RequestParam(required = false) String type, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取[{}]订单列表", ParamHelper.getClientAccountInfo(clientAccount), type);
        OrderQuery query = OrderQuery.builder()
                .type(type)
                .build();
        List<OrderListResult> result = vehicleSaleOrderAppService.search(query);
        return ApiResponse.ok(getPageResult(PageUtil.convert(result, OrderVoAssembler.INSTANCE::toVo)));
    }

    @PostMapping("/wishlist/action/create")
    public ApiResponse<String> createWishlist(@RequestBody @Valid SelectedSaleModelRequestVo request, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]新建心愿单", ParamHelper.getClientAccountInfo(clientAccount));
        CreateWishlistCmd cmd = SelectedSaleModelRequestVoAssembler.INSTANCE.toCreateWishlistCmd(clientAccount.getAccountId(), request);
        String orderNum = vehicleSaleOrderAppService.createUserWishlist(cmd);
        return ApiResponse.ok(orderNum);
    }

    @PostMapping("/wishlist/action/modify")
    public ApiResponse<Void> modifyWishlist(@RequestBody @Valid SelectedSaleModelRequestVo request, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]修改心愿单[{}]", ParamHelper.getClientAccountInfo(clientAccount), request.getOrderNum());
        ModifyWishlistCmd cmd = SelectedSaleModelRequestVoAssembler.INSTANCE.toModifyWishlistCmd(clientAccount.getAccountId(), request);
        vehicleSaleOrderAppService.modifyUserWishlist(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/wishlist/action/delete")
    public ApiResponse<Void> deleteWishlist(@RequestBody @Valid OrderVo order, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]删除心愿单[{}]", ParamHelper.getClientAccountInfo(clientAccount), order.getOrderNum());
        DeleteWishlistCmd cmd = OrderVoAssembler.INSTANCE.toDeleteWishlistCmd(clientAccount.getAccountId(), order);
        vehicleSaleOrderAppService.deleteUserWishlist(cmd);
        return ApiResponse.ok();
    }

    @GetMapping("/wishlist/{orderNum}")
    public ApiResponse<WishlistResponseVo> getWishlist(@PathVariable String orderNum, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取心愿单[{}]详情", ParamHelper.getClientAccountInfo(clientAccount), orderNum);
        WishlistDetailResult result = vehicleSaleOrderAppService.getUserWishlist(clientAccount.getAccountId(), orderNum);
        WishlistResponseVo vo = WishlistResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/action/earnestMoneyOrder")
    public ApiResponse<String> earnestMoneyOrder(@RequestBody @Valid EarnestMoneyOrderRequestVo request, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]意向金下订单", ParamHelper.getClientAccountInfo(clientAccount));
        EarnestMoneyCmd cmd = EarnestMoneyOrderRequestVoAssembler.INSTANCE.toCmd(clientAccount.getAccountId(), request);
        String orderNum = vehicleSaleOrderAppService.earnestMoneyOrder(cmd);
        return ApiResponse.ok(orderNum);
    }

    @PostMapping("/action/downPaymentOrder")
    public ApiResponse<String> downPaymentOrder(@RequestBody @Valid DownPaymentOrderRequestVo request, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]定金下订单", ParamHelper.getClientAccountInfo(clientAccount));
        DownPaymentCmd cmd = DownPaymentOrderRequestVoAssembler.INSTANCE.toCmd(clientAccount.getAccountId(), request);
        String orderNum = vehicleSaleOrderAppService.downPaymentOrder(cmd);
        return ApiResponse.ok(orderNum);
    }

    @GetMapping("/order/{orderNum}")
    public ApiResponse<OrderResponseVo> getOrder(@PathVariable String orderNum, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]获取订单[{}]详情", ParamHelper.getClientAccountInfo(clientAccount), orderNum);
        OrderDetailResult result = vehicleSaleOrderAppService.getUserOrder(clientAccount.getAccountId(), orderNum);
        OrderResponseVo vo = OrderResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/order/action/cancel")
    public ApiResponse<Void> cancel(@RequestBody @Valid OrderVo order, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]取消订单[{}]", ParamHelper.getClientAccountInfo(clientAccount), order.getOrderNum());
        CancelCmd cmd = OrderVoAssembler.INSTANCE.toCancelCmd(clientAccount.getAccountId(), order);
        vehicleSaleOrderAppService.cancel(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/pay")
    public ApiResponse<OrderPaymentResponseVo> pay(@RequestBody @Valid OrderPaymentRequestVo request, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]支付订单[{}]", ParamHelper.getClientAccountInfo(clientAccount), request.getOrderNum());
        PayCmd cmd = OrderPaymentRequestVoAssembler.INSTANCE.toCmd(clientAccount.getAccountId(), request);
        PayResult result = vehicleSaleOrderAppService.pay(cmd);
        OrderPaymentResponseVo vo = OrderPaymentResponseVoAssembler.INSTANCE.toVo(result);
        return ApiResponse.ok(vo);
    }

    @PostMapping("/order/action/requestRefund")
    public ApiResponse<Void> requestRefund(@RequestBody @Valid OrderVo order, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]退款订单[{}]", ParamHelper.getClientAccountInfo(clientAccount), order.getOrderNum());
        RequestRefundCmd cmd = OrderResponseVoAssembler.INSTANCE.toRequestRefundCmd(clientAccount.getAccountId(), order);
        vehicleSaleOrderAppService.requestRefund(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/earnestMoneyToDownPayment")
    public ApiResponse<Void> earnestMoneyToDownPayment(@RequestBody @Valid OrderVo order, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]订单[{}]意向金转定金", ParamHelper.getClientAccountInfo(clientAccount), order.getOrderNum());
        EarnestToDownCmd cmd = OrderVoAssembler.INSTANCE.toEarnestToDownCmd(clientAccount.getAccountId(), order);
        vehicleSaleOrderAppService.earnestMoneyToDownPayment(cmd);
        return ApiResponse.ok();
    }

    @PostMapping("/order/action/lock")
    public ApiResponse<Void> lock(@RequestBody @Valid OrderVo order, @RequestHeader ClientAccount clientAccount) {
        log.info("手机客户端[{}]锁定订单[{}]", ParamHelper.getClientAccountInfo(clientAccount), order.getOrderNum());
        LockCmd cmd = OrderVoAssembler.INSTANCE.toLockCmd(clientAccount.getAccountId(), order);
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
