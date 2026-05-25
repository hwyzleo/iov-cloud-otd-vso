package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.common.util.ParamHelper;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.api.vo.InitiateSupplementPaymentRequestVo;
import net.hwyz.iov.cloud.otd.vso.api.vo.SupplementaryPaymentVo;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 改配补款移动端控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/mobile/vso/v1/supplement")
@RequiredArgsConstructor
public class MobileSupplementPaymentController extends BaseController {

    private final OrderAppService orderAppService;

    /**
     * 获取订单的补款任务列表
     */
    @GetMapping("/list")
    public ApiResponse<List<SupplementaryPaymentVo>> getSupplementaryPayments(@RequestParam String orderNo) {
        log.info("手机客户端[{}]获取补款任务列表, orderNo={}", ParamHelper.getClientAccountInfo(), orderNo);
        String accountId = SecurityContextHolder.getUserId();
        List<SupplementaryPaymentVo> result = orderAppService.getSupplementaryPayments(accountId, orderNo);
        return ApiResponse.ok(result);
    }

    /**
     * 发起补款支付
     */
    @PostMapping("/initiatePayment")
    public ApiResponse<Void> initiateSupplementPayment(@Valid @RequestBody InitiateSupplementPaymentRequestVo request) {
        log.info("手机客户端[{}]发起补款支付, supplementaryNo={}, paymentChannel={}",
                ParamHelper.getClientAccountInfo(), request.getSupplementaryNo(), request.getPaymentChannel());
        String accountId = SecurityContextHolder.getUserId();
        orderAppService.initiateSupplementPayment(accountId, request.getSupplementaryNo(), request.getPaymentChannel());
        return ApiResponse.ok();
    }
}
