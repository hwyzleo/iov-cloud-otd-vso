package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentScene;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentAmountMismatchException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 支付回调应用服务
 * <p>
 * US-004 / INV-003 / INV-005：签名、金额、阶段、状态校验，同一事务写
 * Payment / OrderAmount / Order / Timeline，重复幂等、冲突拒绝。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCallbackService {

    private final PaymentRepository paymentRepository;
    private final CallbackLogRepository callbackLogRepository;
    private final SupplementaryPaymentRepository supplementaryPaymentRepository;
    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final OrderTimelineAppService orderTimelineAppService;
    private final CallbackSignatureService callbackSignatureService;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 处理支付回调
     *
     * @param cmd       回调命令
     * @param signature X-Signature 原文
     * @param timestamp 回调时间戳（Unix 秒）
     * @param nonce     随机串
     */
    @Transactional(rollbackFor = Exception.class)
    public PaymentCallbackResult handleCallback(PaymentCallbackCmd cmd, String signature,
                                                Long timestamp, String nonce) {
        log.info("处理支付回调：paymentNo={}, externalTradeNo={}, paymentStage={}, status={}",
                cmd.getPaymentNo(), cmd.getExternalTradeNo(), cmd.getPaymentStage(), cmd.getPaymentStatus());

        if (!verifySignature(cmd, signature, timestamp, nonce)) {
            saveCallbackLog(null, cmd, buildIdempotentKey(cmd), "FAIL", "签名或防重放校验失败");
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("签名或防重放校验失败")
                    .build();
        }

        Optional<Payment> paymentOpt = paymentRepository.findByPaymentNo(cmd.getPaymentNo());
        if (paymentOpt.isEmpty()) {
            saveCallbackLog(null, cmd, buildIdempotentKey(cmd), "FAIL", "支付单不存在");
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("支付单不存在")
                    .build();
        }
        Payment payment = paymentOpt.get();

        PaymentStage stage = PaymentStage.valOf(cmd.getPaymentStage());
        if (stage == null || !stage.name().equals(payment.getPaymentStage())) {
            saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "FAIL",
                    "支付阶段不匹配：" + cmd.getPaymentStage());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("支付阶段不匹配")
                    .build();
        }

        if (payment.getPaymentAmount().compareTo(cmd.getPaymentAmount()) != 0) {
            saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "FAIL",
                    "金额不一致，期望：" + payment.getPaymentAmount() + "，实际：" + cmd.getPaymentAmount());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("支付金额不一致")
                    .build();
        }

        PaymentStatus targetStatus = parseStatus(cmd.getPaymentStatus());
        if (targetStatus == null) {
            saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "FAIL",
                    "不支持的支付状态：" + cmd.getPaymentStatus());
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.FAIL)
                    .message("不支持的支付状态")
                    .build();
        }

        // 已成功：相同结果幂等返回，不再累计金额或追加时间线
        if (payment.getPaymentStatus() == PaymentStatus.PAID) {
            if (targetStatus == PaymentStatus.PAID) {
                saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "SUCCESS", null);
                return PaymentCallbackResult.builder()
                        .code(PaymentCallbackResultCode.DUPLICATE)
                        .message("该回调已处理")
                        .build();
            }
            saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "FAIL",
                    "支付单已支付成功，拒绝冲突状态：" + targetStatus);
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.CONFLICT)
                    .message("支付单已成功，冲突状态被拒绝")
                    .build();
        }

        if (payment.getPaymentStatus() == PaymentStatus.CANCELLED && targetStatus == PaymentStatus.PAID) {
            saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "FAIL",
                    "支付单已取消后收到支付成功，需人工核查");
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.CONFLICT)
                    .message("支付单已取消后收到成功回调，已记录人工核查，不恢复状态")
                    .build();
        }

        switch (targetStatus) {
            case PAID -> handlePaid(payment, stage, cmd);
            case PROCESSING -> handleProcessing(payment, cmd);
            case PAYMENT_FAILED -> handleFailed(payment, cmd);
            case CANCELLED -> handleCancelled(payment, cmd);
            default -> {
                return PaymentCallbackResult.builder()
                        .code(PaymentCallbackResultCode.FAIL)
                        .message("不支持的支付状态")
                        .build();
            }
        }

        saveCallbackLog(payment.getOrderId(), cmd, buildIdempotentKey(cmd), "SUCCESS", null);
        return PaymentCallbackResult.builder()
                .code(PaymentCallbackResultCode.SUCCESS)
                .message("回调处理成功")
                .build();
    }

    private void handlePaid(Payment payment, PaymentStage stage, PaymentCallbackCmd cmd) {
        Payment local = paymentRepository.findByPaymentNo(payment.getPaymentNo())
                .orElseThrow(() -> new PaymentNotExistException(payment.getPaymentNo()));
        local.markPaid(cmd.getExternalTradeNo(), cmd.getPayTime());
        paymentRepository.save(local);

        // 补款（意向金转定金差额）
        Optional<SupplementaryPaymentPo> supplementOpt =
                supplementaryPaymentRepository.findByPaymentId(payment.getPaymentNo());
        if (supplementOpt.isPresent()) {
            SupplementaryPaymentPo supplement = supplementOpt.get();
            if (!SupplementaryPaymentStatus.COMPLETED.equals(supplement.getSupplementaryStatus())) {
                orderRepository.findByOrderId(payment.getOrderId()).ifPresent(order -> {
                    OrderAmount amount = orderAmountRepository.findDomainByOrderId(order.getId()).orElse(null);
                    if (amount == null) {
                        throw new net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException(
                                "订单[" + order.getOrderNo() + "]金额快照缺失，需人工修复");
                    }
                    amount.addPaidAmount(new Money(payment.getPaymentAmount()));
                    order.setOrderAmount(amount);
                    if (SupplementaryPaymentScene.EARNEST_TO_DOWN.getValue().equals(supplement.getSupplementaryScene())
                            && order.getOrderState() == OrderState.EARNEST_MONEY_PAID) {
                        order.earnestMoneyToDownPayment();
                        orderTimelineAppService.record(order.getId(),
                                OrderTimelineEventType.EARNEST_TO_DOWN_COMPLETED,
                                String.valueOf(OrderState.EARNEST_MONEY_PAID.getValue()),
                                String.valueOf(order.getOrderState().getValue()),
                                "system", "system", "payment_system",
                                supplement.getSupplementaryNo(), null,
                                "意向金转定金差额支付成功，补款金额：" + supplement.getSupplementaryAmount());
                    }
                    orderRepository.save(order);
                });
                supplementaryPaymentRepository.updateStatus(supplement.getSupplementaryNo(),
                        SupplementaryPaymentStatus.COMPLETED, payment.getPaymentNo());
            }
            orderTimelineAppService.record(payment.getOrderId(), OrderTimelineEventType.PAYMENT_SUCCESS,
                    null, null, "system", "system", "payment_system", payment.getPaymentNo(), null,
                    "补款支付成功，金额：" + payment.getPaymentAmount());
            publishPaymentSuccessEvent(payment, stage, cmd);
            return;
        }

        Order order = orderRepository.findByOrderId(payment.getOrderId())
                .orElseThrow(() -> new PaymentConflictException(
                        "支付单[" + payment.getPaymentNo() + "]关联订单不存在"));
        String beforeState = String.valueOf(order.getOrderState().getValue());
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(order.getId())
                .orElseThrow(() -> new net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException(
                        "订单[" + order.getOrderNo() + "]金额快照缺失，需人工修复"));

        order.pay(payment.getPaymentAmount());
        amount.addPaidAmount(new Money(payment.getPaymentAmount()));
        order.setOrderAmount(amount);
        orderRepository.save(order);

        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.PAYMENT_SUCCESS,
                beforeState, String.valueOf(order.getOrderState().getValue()),
                "system", "system", "payment_system", payment.getPaymentNo(), null,
                "支付成功，阶段：" + stage + "，金额：" + payment.getPaymentAmount());

        publishPaymentSuccessEvent(payment, stage, cmd);
    }

    /**
     * 发布支付成功领域事件（OTD-VSO-DSN-CR-005）
     * <p>
     * 由 PaymentSuccessEventListener 在事务提交后取消小订单支付超时任务，
     * 避免已支付订单被超时任务误置为失效。
     */
    private void publishPaymentSuccessEvent(Payment payment, PaymentStage stage, PaymentCallbackCmd cmd) {
        eventPublisher.publishEvent(PaymentSuccessDomainEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getPaymentNo())
                .paymentStage(stage)
                .paymentAmount(payment.getPaymentAmount())
                .payTime(cmd.getPayTime() != null ? cmd.getPayTime() : LocalDateTime.now())
                .occurTime(LocalDateTime.now())
                .build());
    }

    private void handleProcessing(Payment payment, PaymentCallbackCmd cmd) {
        Payment local = paymentRepository.findByPaymentNo(payment.getPaymentNo()).orElse(payment);
        local.markProcessing(cmd.getExternalTradeNo());
        paymentRepository.save(local);
        orderTimelineAppService.record(payment.getOrderId(), OrderTimelineEventType.PAYMENT_PROCESSING,
                null, null, "system", "system", "payment_system", payment.getPaymentNo(), null,
                "支付处理中");
    }

    private void handleFailed(Payment payment, PaymentCallbackCmd cmd) {
        Payment local = paymentRepository.findByPaymentNo(payment.getPaymentNo()).orElse(payment);
        local.markFailed(cmd.getFailReason());
        paymentRepository.save(local);
        orderTimelineAppService.record(payment.getOrderId(), OrderTimelineEventType.PAYMENT_FAILED,
                null, null, "system", "system", "payment_system", payment.getPaymentNo(),
                cmd.getFailReason() == null ? "支付失败" : cmd.getFailReason(), "支付失败，订单保持待支付");
    }

    private void handleCancelled(Payment payment, PaymentCallbackCmd cmd) {
        Payment local = paymentRepository.findByPaymentNo(payment.getPaymentNo()).orElse(payment);
        local.markCancelled(cmd.getFailReason());
        paymentRepository.save(local);
        orderTimelineAppService.record(payment.getOrderId(), OrderTimelineEventType.PAYMENT_CANCELLED,
                null, null, "system", "system", "payment_system", payment.getPaymentNo(),
                cmd.getFailReason() == null ? "支付已取消" : cmd.getFailReason(), "支付已取消，订单保持待支付");
    }

    private boolean verifySignature(PaymentCallbackCmd cmd, String signature, Long timestamp, String nonce) {
        Map<String, String> signData = new HashMap<>();
        signData.put("amount", cmd.getPaymentAmount() == null ? "" : cmd.getPaymentAmount().toPlainString());
        signData.put("orderId", cmd.getPaymentNo());
        signData.put("paySeq", cmd.getExternalTradeNo());
        signData.put("status", cmd.getPaymentStatus());
        return callbackSignatureService.verifySignature(signature, timestamp, nonce, signData);
    }

    private PaymentStatus parseStatus(String status) {
        for (PaymentStatus value : PaymentStatus.values()) {
            if (value.name().equals(status)) {
                return value;
            }
        }
        return null;
    }

    private String buildIdempotentKey(PaymentCallbackCmd cmd) {
        if (cmd.getIdempotentKey() != null && !cmd.getIdempotentKey().isEmpty()) {
            return cmd.getIdempotentKey();
        }
        return "PAY:" + cmd.getExternalTradeNo();
    }

    private void saveCallbackLog(String orderId, PaymentCallbackCmd cmd, String idempotentKey,
                                 String processResult, String failReason) {
        CallbackLogPo logPo = new CallbackLogPo();
        logPo.setCallbackLogId(IdUtil.nanoId(15));
        logPo.setOrderId(orderId);
        logPo.setBusinessType("PAYMENT");
        logPo.setExternalSystemName("MOCK_PAY");
        logPo.setExternalBusinessNo(cmd.getExternalTradeNo());
        logPo.setIdempotentKey(idempotentKey);
        logPo.setCallbackStatusValue(cmd.getPaymentStatus());
        logPo.setCallbackResultCode(cmd.getPaymentStatus());
        logPo.setEventTime(cmd.getPayTime());
        logPo.setRequestBody(serialize(cmd));
        logPo.setProcessResult(processResult);
        logPo.setFailReason(failReason);
        logPo.setProcessTime(LocalDateTime.now());
        logPo.setManualOverrideFlag(0);
        callbackLogRepository.save(logPo);
    }

    private String serialize(PaymentCallbackCmd cmd) {
        try {
            return objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            log.warn("序列化支付回调请求失败", e);
            return "{}";
        }
    }
}
