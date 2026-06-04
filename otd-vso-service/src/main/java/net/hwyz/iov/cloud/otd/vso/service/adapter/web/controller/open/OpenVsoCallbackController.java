package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.open;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.framework.common.bean.ApiResponse;
import net.hwyz.iov.cloud.framework.web.controller.BaseController;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.request.PaymentCallbackRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.CallbackSignatureService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.PaymentCallbackService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 开放回调接口（外部系统回调）
 *
 * @author VSO Team
 */
@Slf4j
@RestController
@RequestMapping("/api/open/vsoCallback/v1")
@RequiredArgsConstructor
public class OpenVsoCallbackController extends BaseController {

    private final PaymentCallbackService paymentCallbackService;
    private final CallbackSignatureService callbackSignatureService;

    /**
     * 支付回调
     */
    @PostMapping("/payment")
    public ApiResponse<Void> paymentCallback(@Valid @RequestBody PaymentCallbackRequest request,
                                             @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("支付回调：paymentNo={}, externalTradeNo={}, paymentStage={}",
                request.getPaymentNo(), request.getExternalTradeNo(), request.getPaymentStage());
// 手机端自行模拟，先不验证
//        if (!verifySignature(signature, request)) {
//            log.warn("支付回调签名验证失败：paymentNo={}", request.getPaymentNo());
//            return ApiResponse.fail("签名验证失败");
//        }

        try {
            PaymentCallbackCmd cmd = PaymentCallbackCmd.builder()
                    .paymentNo(request.getPaymentNo())
                    .externalTradeNo(request.getExternalTradeNo())
                    .paymentStage(request.getPaymentStage())
                    .paymentAmount(request.getPaymentAmount())
                    .paymentStatus(request.getPaymentStatus())
                    .payTime(request.getPayTime())
                    .idempotentKey(request.getIdempotentKey())
                    .build();

            PaymentCallbackResult result = paymentCallbackService.handleCallback(cmd);

            if (result.getCode() == PaymentCallbackResultCode.SUCCESS) {
                return ApiResponse.ok();
            } else if (result.getCode() == PaymentCallbackResultCode.DUPLICATE) {
                return ApiResponse.ok();
            } else {
                return ApiResponse.fail(result.getMessage());
            }
        } catch (Exception e) {
            log.error("支付回调异常", e);
            return ApiResponse.fail("回调处理失败");
        }
    }

    private boolean verifySignature(String signature, PaymentCallbackRequest request) {
        Map<String, String> signData = new HashMap<>();
        signData.put("amount", request.getPaymentAmount() != null ? request.getPaymentAmount().toPlainString() : "");
        signData.put("orderId", request.getPaymentNo());
        signData.put("paySeq", request.getExternalTradeNo());
        signData.put("status", request.getPaymentStatus());
        return callbackSignatureService.verifySignature(signature, request.getTimestamp(), request.getNonce(), signData);
    }

    /**
     * 电子签回调
     */
    @PostMapping("/contract")
    public ApiResponse<Void> contractCallback(@RequestBody Map<String, Object> payload,
                                              @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("电子签回调：payload={}", payload);

        try {
            // TODO: 1. 验证签名
            // TODO: 2. 幂等处理
            // TODO: 3. 更新合同状态

            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("电子签回调异常", e);
            return ApiResponse.fail("回调处理失败");
        }
    }

    /**
     * 金融回调
     */
    @PostMapping("/finance")
    public ApiResponse<Void> financeCallback(@RequestBody Map<String, Object> payload,
                                             @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("金融回调：payload={}", payload);

        try {
            // TODO: 1. 验证签名
            // TODO: 2. 幂等处理
            // TODO: 3. 更新金融状态

            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("金融回调异常", e);
            return ApiResponse.fail("回调处理失败");
        }
    }

    /**
     * 补贴回调
     */
    @PostMapping("/subsidy")
    public ApiResponse<Void> subsidyCallback(@RequestBody Map<String, Object> payload,
                                             @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("补贴回调：payload={}", payload);

        try {
            // TODO: 1. 验证签名
            // TODO: 2. 幂等处理
            // TODO: 3. 更新补贴状态

            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("补贴回调异常", e);
            return ApiResponse.fail("回调处理失败");
        }
    }

    /**
     * 发票回调
     */
    @PostMapping("/invoice")
    public ApiResponse<Void> invoiceCallback(@RequestBody Map<String, Object> payload,
                                             @RequestHeader(value = "X-Signature", required = false) String signature) {
        log.info("发票回调：payload={}", payload);

        try {
            // TODO: 1. 验证签名
            // TODO: 2. 幂等处理
            // TODO: 3. 更新发票状态

            return ApiResponse.ok();
        } catch (Exception e) {
            log.error("发票回调异常", e);
            return ApiResponse.fail("回调处理失败");
        }
    }

}
