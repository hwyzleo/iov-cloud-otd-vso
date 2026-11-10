package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.RefundCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 退款回调应用服务
 * <p>
 * US-006 / INV-003：签名、金额、主从单校验；明细终态幂等，乱序回调聚合主单。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundCallbackService {

    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final RefundAppService refundAppService;
    private final OrderTimelineAppService orderTimelineAppService;
    private final CallbackSignatureService callbackSignatureService;

    @Transactional(rollbackFor = Exception.class)
    public PaymentCallbackResult handleCallback(RefundCallbackCmd cmd, String signature,
                                                Long timestamp, String nonce) {
        log.info("处理退款回调：refundNo={}, refundItemId={}, status={}",
                cmd.getRefundNo(), cmd.getRefundItemId(), cmd.getRefundStatus());

        if (!verifySignature(cmd, signature, timestamp, nonce)) {
            return fail("签名或防重放校验失败");
        }

        Refund refund = refundRepository.findByRefundNo(cmd.getRefundNo()).orElse(null);
        if (refund == null) {
            return fail("退款单不存在");
        }
        RefundItem item = refund.getItems().stream()
                .filter(candidate -> candidate.getRefundItemId().equals(cmd.getRefundItemId()))
                .findFirst()
                .orElse(null);
        if (item == null) {
            return fail("退款明细不属于该退款主单");
        }
        if (item.getRefundAmount().compareTo(cmd.getRefundAmount()) != 0) {
            return fail("退款明细金额不一致");
        }

        RefundStatus targetStatus = RefundStatus.fromValue(cmd.getRefundStatus());
        if (targetStatus == null) {
            return fail("不支持的退款状态：" + cmd.getRefundStatus());
        }

        if (item.getRefundStatus() == RefundStatus.COMPLETED) {
            if (targetStatus == RefundStatus.COMPLETED) {
                return PaymentCallbackResult.builder()
                        .code(PaymentCallbackResultCode.DUPLICATE)
                        .message("退款明细已完成，幂等返回")
                        .build();
            }
            return PaymentCallbackResult.builder()
                    .code(PaymentCallbackResultCode.CONFLICT)
                    .message("退款明细已完成，冲突状态被拒绝")
                    .build();
        }

        switch (targetStatus) {
            case PROCESSING -> item.markProcessing(cmd.getExternalRefundNo());
            case COMPLETED -> item.markCompleted(cmd.getRefundTime());
            case FAILED -> item.markFailed(cmd.getFailReason(), cmd.getRefundTime());
            case PENDING -> {
                return fail("退款回调不支持 pending 状态");
            }
        }
        refundItemRepository.save(item);

        List<RefundItem> items = refundItemRepository.findByRefundId(refund.getRefundId());
        refund.setItems(items);
        refund.aggregateStatus();
        refundRepository.updateStatus(refund);

        orderTimelineAppService.record(refund.getOrderId(), mapEventType(refund.getRefundStatus()),
                null, null, "system", "system", "payment_system", refund.getRefundNo(),
                refund.getRefundStatus() == RefundStatus.FAILED ? refund.getFailReason() : null,
                "退款明细[" + item.getRefundItemId() + "]状态：" + item.getRefundStatus().getValue());

        if (refund.getRefundStatus() == RefundStatus.COMPLETED) {
            refundAppService.completeOrderRefund(refund);
        }

        return PaymentCallbackResult.builder()
                .code(PaymentCallbackResultCode.SUCCESS)
                .message("退款回调处理成功")
                .build();
    }

    private OrderTimelineEventType mapEventType(RefundStatus status) {
        return switch (status) {
            case COMPLETED -> OrderTimelineEventType.REFUND_COMPLETED;
            case FAILED -> OrderTimelineEventType.REFUND_FAILED;
            default -> OrderTimelineEventType.REFUND_PROCESSING;
        };
    }

    private boolean verifySignature(RefundCallbackCmd cmd, String signature, Long timestamp, String nonce) {
        Map<String, String> signData = new HashMap<>();
        signData.put("amount", cmd.getRefundAmount() == null ? "" : cmd.getRefundAmount().toPlainString());
        signData.put("orderId", cmd.getRefundItemId());
        signData.put("paySeq", cmd.getExternalRefundNo());
        signData.put("refundNo", cmd.getRefundNo());
        signData.put("status", cmd.getRefundStatus());
        return callbackSignatureService.verifySignature(signature, timestamp, nonce, signData);
    }

    private PaymentCallbackResult fail(String message) {
        log.warn("退款回调处理失败：{}", message);
        return PaymentCallbackResult.builder()
                .code(PaymentCallbackResultCode.FAIL)
                .message(message)
                .build();
    }
}
