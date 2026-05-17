package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.PaymentPo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final CallbackLogRepository callbackLogRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional(rollbackFor = Exception.class)
    public PaymentCallbackResult handleCallback(PaymentCallbackCmd cmd) {
        log.info("处理支付回调：paymentNo={}, externalTradeNo={}, paymentStage={}",
                cmd.getPaymentNo(), cmd.getExternalTradeNo(), cmd.getPaymentStage());

        String idempotentKey = buildIdempotentKey(cmd);
        String requestBody = serializeRequestBody(cmd);

        Optional<CallbackLogPo> existingLog = callbackLogRepository.findByIdempotentKey(idempotentKey);
        if (existingLog.isPresent()) {
            CallbackLogPo logPo = existingLog.get();
            if ("SUCCESS".equals(logPo.getProcessResult())) {
                log.info("回调已处理，幂等返回：idempotentKey={}", idempotentKey);
                return PaymentCallbackResult.builder()
                        .code(PaymentCallbackResultCode.DUPLICATE)
                        .message("该回调已处理")
                        .build();
            }
        }

        Optional<PaymentPo> paymentOpt = paymentRepository.findByPaymentNo(cmd.getPaymentNo());
        if (paymentOpt.isEmpty()) {
            log.warn("支付单不存在：paymentNo={}", cmd.getPaymentNo());
            saveCallbackLog(null, cmd, idempotentKey, requestBody, "FAIL", "支付单不存在");
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("支付单不存在")
                    .build();
        }

        PaymentPo paymentPo = paymentOpt.get();
        if (!PaymentStatus.PENDING_PAYMENT.name().equals(paymentPo.getPaymentStatus())) {
            log.warn("支付单状态不匹配：paymentNo={}, currentStatus={}",
                    cmd.getPaymentNo(), paymentPo.getPaymentStatus());
            saveCallbackLog(paymentPo.getOrderId(), cmd, idempotentKey, requestBody, "FAIL",
                    "支付单状态不匹配，当前状态：" + paymentPo.getPaymentStatus());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("支付单状态不匹配，当前状态：" + paymentPo.getPaymentStatus())
                    .build();
        }

        if (paymentPo.getPaymentAmount().compareTo(cmd.getPaymentAmount()) != 0) {
            log.warn("支付金额不一致：paymentNo={}, expected={}, actual={}",
                    cmd.getPaymentNo(), paymentPo.getPaymentAmount(), cmd.getPaymentAmount());
            saveCallbackLog(paymentPo.getOrderId(), cmd, idempotentKey, requestBody, "FAIL",
                    "金额不一致，期望：" + paymentPo.getPaymentAmount() + "，实际：" + cmd.getPaymentAmount());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("金额不一致，期望：" + paymentPo.getPaymentAmount() + "，实际：" + cmd.getPaymentAmount())
                    .build();
        }

        PaymentStage paymentStage = PaymentStage.valOf(cmd.getPaymentStage());
        if (paymentStage == null) {
            log.warn("不支持的支付阶段：paymentStage={}", cmd.getPaymentStage());
            saveCallbackLog(paymentPo.getOrderId(), cmd, idempotentKey, requestBody, "FAIL",
                    "不支持的支付阶段：" + cmd.getPaymentStage());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("不支持的支付阶段：" + cmd.getPaymentStage())
                    .build();
        }

        CallbackLogPo callbackLog = createCallbackLog(paymentPo.getOrderId(), cmd, idempotentKey, requestBody, "PROCESSING");
        callbackLogRepository.save(callbackLog);

        paymentRepository.updateStatus(cmd.getPaymentNo(), PaymentStatus.PAID.name(),
                cmd.getExternalTradeNo(), cmd.getPayTime());

        PaymentSuccessDomainEvent event = PaymentSuccessDomainEvent.builder()
                .orderId(paymentPo.getOrderId())
                .paymentId(paymentPo.getPaymentId())
                .paymentStage(paymentStage)
                .paymentAmount(cmd.getPaymentAmount())
                .payTime(cmd.getPayTime())
                .occurTime(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(event);

        callbackLog.setProcessResult("SUCCESS");
        callbackLog.setProcessTime(LocalDateTime.now());
        callbackLogRepository.save(callbackLog);

        log.info("支付回调处理成功：paymentNo={}, orderId={}", cmd.getPaymentNo(), paymentPo.getOrderId());
        return PaymentCallbackResult.builder()
                .code(PaymentCallbackResultCode.SUCCESS)
                .message("回调处理成功")
                .build();
    }

    private String buildIdempotentKey(PaymentCallbackCmd cmd) {
        if (cmd.getIdempotentKey() != null && !cmd.getIdempotentKey().isEmpty()) {
            return cmd.getIdempotentKey();
        }
        return "PAY:" + cmd.getExternalTradeNo();
    }

    private String serializeRequestBody(PaymentCallbackCmd cmd) {
        try {
            return objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            log.warn("序列化请求失败", e);
            return "{}";
        }
    }

    private void saveCallbackLog(String orderId, PaymentCallbackCmd cmd, String idempotentKey,
                                 String requestBody, String processResult, String failReason) {
        CallbackLogPo logPo = createCallbackLog(orderId, cmd, idempotentKey, requestBody, processResult);
        if ("FAIL".equals(processResult)) {
            logPo.setFailReason(failReason);
        }
        callbackLogRepository.save(logPo);
    }

    private CallbackLogPo createCallbackLog(String orderId, PaymentCallbackCmd cmd,
                                            String idempotentKey, String requestBody, String processResult) {
        CallbackLogPo logPo = new CallbackLogPo();
        logPo.setCallbackLogId(IdUtil.nanoId(15));
        logPo.setOrderId(orderId);
        logPo.setBusinessType("PAYMENT");
        logPo.setExternalSystemName("MOCK_PAY");
        logPo.setExternalBusinessNo(cmd.getExternalTradeNo());
        logPo.setIdempotentKey(idempotentKey);
        logPo.setCallbackStatusValue(cmd.getPaymentStatus());
        logPo.setCallbackResultCode(PaymentStatus.PAID.name());
        logPo.setEventTime(cmd.getPayTime());
        logPo.setRequestBody(requestBody);
        logPo.setProcessResult(processResult);
        logPo.setManualOverrideFlag(0);
        return logPo;
    }

}