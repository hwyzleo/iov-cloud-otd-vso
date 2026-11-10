package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.RefundAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * C 端支付与退款接口（OTD-VSO-DSN-CR-002 US-004 / US-006）
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mobile/vso/v1")
public class MobilePaymentController extends BaseController {

    private final PaymentAppService paymentAppService;
    private final RefundAppService refundAppService;

    /**
     * 查询支付单
     */
    @GetMapping("/order/{orderNo}/payment/{paymentNo}")
    public ApiResponse<PaymentResult> getPayment(@PathVariable String orderNo,
                                                 @PathVariable String paymentNo) {
        return ApiResponse.ok(paymentAppService.getPayment(SecurityContextHolder.getUserId(), orderNo, paymentNo));
    }

    /**
     * 查询本人订单支付记录
     */
    @GetMapping("/order/{orderNo}/payments")
    public ApiResponse<List<PaymentResult>> listPayments(@PathVariable String orderNo) {
        return ApiResponse.ok(paymentAppService.listPayments(SecurityContextHolder.getUserId(), orderNo));
    }

    /**
     * 发起退款（返回主单与明细进度）
     */
    @PostMapping("/order/action/refund")
    public ApiResponse<RefundResult> refund(@RequestBody OrderNoRequest request) {
        return ApiResponse.ok(refundAppService.apply(SecurityContextHolder.getUserId(), request.getOrderNo()));
    }

    /**
     * 查询退款进度
     */
    @GetMapping("/refund/{refundNo}")
    public ApiResponse<RefundResult> getRefund(@PathVariable String refundNo) {
        return ApiResponse.ok(refundAppService.getRefund(SecurityContextHolder.getUserId(), refundNo));
    }

    /**
     * 失败退款重试
     */
    @PostMapping("/refund/action/retry")
    public ApiResponse<RefundResult> retryRefund(@RequestBody RefundNoRequest request) {
        return ApiResponse.ok(refundAppService.retry(SecurityContextHolder.getUserId(), request.getRefundNo()));
    }

    /**
     * 订单号请求体
     */
    @lombok.Data
    public static class OrderNoRequest {
        private String orderNo;
    }

    /**
     * 退款单号请求体
     */
    @lombok.Data
    public static class RefundNoRequest {
        private String refundNo;
    }
}
