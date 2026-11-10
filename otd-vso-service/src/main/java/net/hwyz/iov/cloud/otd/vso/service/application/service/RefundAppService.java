package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.RefundResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.RefundAmountInvalidException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.RefundNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.RefundStatusNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.RefundGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Refund;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundItem;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.RefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundItemRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.RefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 退款应用服务
 * <p>
 * US-006 / INV-002 / INV-004 / INV-005：
 * 退款主单 + 按原支付单拆分明细；主单金额等于明细之和；
 * 全部明细完成才完成主单与订单；失败允许重试。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefundAppService {

    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;
    private final OrderLockService orderLockService;
    private final PaymentAdapter paymentAdapter;
    private final OrderTimelineAppService orderTimelineAppService;
    private final PlatformTransactionManager transactionManager;

    /**
     * 申请退款
     */
    public RefundResult apply(String accountId, String orderNo) {
        return orderLockService.executeWithLock(orderNo, accountId, "refund",
                () -> doApply(accountId, orderNo));
    }

    private RefundResult doApply(String accountId, String orderNo) {
        Order order = loadOrder(accountId, orderNo);

        // 幂等：存在活动/失败退款主单时返回已有单
        var existing = refundRepository.findActiveByOrderId(order.getId());
        if (existing.isPresent()) {
            log.info("订单存在未完结退款主单，幂等返回：orderNo={}, refundNo={}",
                    orderNo, existing.get().getRefundNo());
            return toResult(order, existing.get());
        }

        if (!order.getOrderAmount().canRefund(order.getOrderState())) {
            throw new OrderStateNotAllowedException(
                    "车辆销售订单[" + orderNo + "]当前状态[" + order.getOrderState() + "]不允许退款");
        }

        Money refundAmount = order.getOrderAmount().calculateRefundAmount(order.getOrderState());
        if (refundAmount.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RefundAmountInvalidException("退款金额必须大于 0：" + refundAmount.getAmount());
        }
        Money paidTotal = order.getOrderAmount().getPaidTotal();
        Money fee = paidTotal.subtract(refundAmount);
        String refundScene = order.getOrderAmount().getRefundScene(order.getOrderState());

        // 按原支付单拆分（支付成功时间正序）
        List<Payment> payments = paymentRepository.findSuccessfulByOrderId(order.getId());
        if (payments.isEmpty()) {
            throw new OrderAmountSnapshotMissingException(
                    "订单[" + orderNo + "]不存在成功支付记录，无法分摊退款");
        }
        List<RefundItem> items = allocateItems(payments, refundAmount.getAmount());
        BigDecimal itemTotal = items.stream().map(RefundItem::getRefundAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (itemTotal.compareTo(refundAmount.getAmount()) != 0) {
            throw new RefundAmountInvalidException(
                    "退款金额[" + refundAmount.getAmount() + "]无法按原支付单完整分摊，明细合计[" + itemTotal + "]");
        }

        String refundId = IdUtil.nanoId(15);
        items.forEach(item -> item.assignRefund(refundId));
        Refund refund = Refund.builder()
                .refundId(refundId)
                .refundNo("REF" + IdUtil.nanoId(12))
                .orderId(order.getId())
                .refundScene(refundScene)
                .refundAmount(refundAmount.getAmount())
                .refundFee(fee.getAmount())
                .refundStatus(RefundStatus.PENDING)
                .applyTime(LocalDateTime.now())
                .items(items)
                .build();
        refund.validateAmount();

        String beforeState = String.valueOf(order.getOrderState().getValue());

        // TX1：退款主从单 + 订单状态 + 时间线
        inTransaction(() -> {
            Order current = orderRepository.findByOrderId(order.getId())
                    .orElseThrow(() -> new OrderNotExistException(orderNo));
            current.requestRefund();
            orderRepository.save(current);
            refundRepository.save(refund);
            orderTimelineAppService.record(current.getId(), OrderTimelineEventType.REFUND_APPLIED,
                    beforeState, String.valueOf(current.getOrderState().getValue()),
                    accountId, "order_user", "capp", refund.getRefundNo(), null,
                    "退款金额：" + refundAmount.getAmount() + "，手续费：" + fee.getAmount());
            return refund;
        });

        log.info("退款申请成功：orderNo={}, refundNo={}, refundAmount={}, fee={}, items={}",
                orderNo, refund.getRefundNo(), refundAmount.getAmount(), fee.getAmount(), items.size());

        // TX1 提交后逐项调用 Mock 退款网关
        Refund processed = dispatchItems(refund, items);
        return toResult(order, processed);
    }

    /**
     * 查询退款进度
     */
    public RefundResult getRefund(String accountId, String refundNo) {
        Refund refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new RefundNotExistException(refundNo));
        Order order = orderRepository.findByOrderIdAndAccountId(refund.getOrderId(), accountId)
                .orElseThrow(() -> new OrderNotExistException(refundNo));
        return toResult(order, refund);
    }

    /**
     * 运营（MPT）按业务订单 ID 查询退款记录（只读，不做账号隔离）
     */
    public List<RefundResult> listByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        return refundRepository.findByOrderId(order.getId()).stream()
                .map(refund -> toResult(order, refund))
                .collect(Collectors.toList());
    }

    /**
     * 失败/中断明细重试
     */
    public RefundResult retry(String accountId, String refundNo) {
        Refund refund = refundRepository.findByRefundNo(refundNo)
                .orElseThrow(() -> new RefundNotExistException(refundNo));
        Order order = orderRepository.findByOrderIdAndAccountId(refund.getOrderId(), accountId)
                .orElseThrow(() -> new OrderNotExistException(refundNo));
        if (refund.getRefundStatus() == RefundStatus.COMPLETED) {
            throw new RefundStatusNotAllowedException("退款单[" + refundNo + "]已完成，无需重试");
        }
        List<RefundItem> retryable = refund.getItems().stream()
                .filter(RefundItem::retryable)
                .collect(Collectors.toList());
        if (retryable.isEmpty()) {
            throw new RefundStatusNotAllowedException("退款单[" + refundNo + "]无可重试明细");
        }
        Refund processed = dispatchItems(refund, retryable);
        return toResult(order, processed);
    }

    /**
     * 逐项调用退款网关并落库（TX2）
     */
    private Refund dispatchItems(Refund refund, List<RefundItem> items) {
        List<RefundItem> refreshed = new ArrayList<>();
        for (RefundItem item : items) {
            RefundGatewayResult gatewayResult;
            try {
                gatewayResult = paymentAdapter.refundItem(item.getRefundItemId(), item.getPaymentNo(),
                        item.getRefundAmount(), "C 端退款");
            } catch (Exception e) {
                log.error("退款网关调用失败：refundItemId={}", item.getRefundItemId(), e);
                refreshed.add(inTransaction(() -> {
                    RefundItem current = refundItemRepository.findByRefundItemId(item.getRefundItemId())
                            .orElse(item);
                    current.markFailed("外部退款网关调用失败：" + e.getMessage(), LocalDateTime.now());
                    refundItemRepository.save(current);
                    return current;
                }));
                continue;
            }
            refreshed.add(inTransaction(() -> {
                RefundItem current = refundItemRepository.findByRefundItemId(item.getRefundItemId())
                        .orElse(item);
                current.markProcessing(gatewayResult.getExternalRefundNo());
                refundItemRepository.save(current);
                return current;
            }));
        }

        return inTransaction(() -> {
            Refund current = refundRepository.findByRefundNo(refund.getRefundNo())
                    .orElse(refund);
            current.setItems(refundItemRepository.findByRefundId(current.getRefundId()));
            current.aggregateStatus();
            refundRepository.updateStatus(current);
            orderTimelineAppService.record(current.getOrderId(),
                    current.getRefundStatus() == RefundStatus.FAILED
                            ? OrderTimelineEventType.REFUND_FAILED
                            : OrderTimelineEventType.REFUND_PROCESSING,
                    OrderStateRef.REFUND_APPLY, OrderStateRef.REFUND_APPLY,
                    "system", "system", "payment_system", current.getRefundNo(),
                    current.getRefundStatus() == RefundStatus.FAILED ? current.getFailReason() : null,
                    "退款明细状态：" + current.getRefundStatus().getValue());
            return current;
        });
    }

    /**
     * 退款完成后的订单推进：REFUND_COMPLETE + refundTotal（按 refundId 幂等）
     */
    public void completeOrderRefund(Refund refund) {
        Order order = orderRepository.findByOrderId(refund.getOrderId())
                .orElseThrow(() -> new OrderNotExistException(refund.getRefundNo()));
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(order.getId())
                .orElseThrow(() -> new OrderAmountSnapshotMissingException(
                        "订单[" + order.getOrderNo() + "]金额快照缺失，需人工修复"));
        String before = String.valueOf(order.getOrderState().getValue());
        if (amount.getRefundTotal().getAmount().compareTo(refund.getRefundAmount()) >= 0) {
            log.info("退款已累计，跳过重复累计：refundNo={}", refund.getRefundNo());
            return;
        }
        amount.setRefundTotal(new Money(amount.getRefundTotal().getAmount().add(refund.getRefundAmount())));
        order.setOrderAmount(amount);
        order.refundComplete();
        orderRepository.save(order);
        orderTimelineAppService.record(order.getId(), OrderTimelineEventType.REFUND_COMPLETED,
                before, String.valueOf(order.getOrderState().getValue()),
                "system", "system", "payment_system", refund.getRefundNo(), null,
                "退款完成，退款金额：" + refund.getRefundAmount());
    }

    private List<RefundItem> allocateItems(List<Payment> payments, BigDecimal refundAmount) {
        List<RefundItem> items = new ArrayList<>();
        BigDecimal remaining = refundAmount;
        for (Payment payment : payments) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal allocated = payment.getPaymentAmount().min(remaining);
            if (allocated.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            items.add(RefundItem.builder()
                    .refundItemId(IdUtil.nanoId(15))
                    .paymentId(payment.getPaymentId())
                    .paymentNo(payment.getPaymentNo())
                    .refundAmount(allocated)
                    .refundStatus(RefundStatus.PENDING)
                    .build());
            remaining = remaining.subtract(allocated);
        }
        return items;
    }

    private Order loadOrder(String accountId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndAccountId(orderNo, accountId)
                .orElseThrow(() -> new OrderNotExistException(orderNo));
        OrderAmount amount = orderAmountRepository.findDomainByOrderId(order.getId())
                .orElseThrow(() -> new OrderAmountSnapshotMissingException(
                        "订单[" + orderNo + "]金额快照缺失，需人工修复"));
        order.setOrderAmount(amount);
        return order;
    }

    private RefundResult toResult(Order order, Refund refund) {
        List<RefundResult.Item> items = refund.getItems() == null ? List.of() : refund.getItems().stream()
                .map(item -> RefundResult.Item.builder()
                        .refundItemId(item.getRefundItemId())
                        .paymentNo(item.getPaymentNo())
                        .refundAmount(item.getRefundAmount())
                        .refundStatus(item.getRefundStatus() == null ? null : item.getRefundStatus().getValue())
                        .externalRefundNo(item.getExternalRefundNo())
                        .failReason(item.getFailReason())
                        .build())
                .collect(Collectors.toList());
        return RefundResult.builder()
                .refundNo(refund.getRefundNo())
                .orderNo(order.getOrderNo())
                .refundScene(refund.getRefundScene())
                .refundAmount(refund.getRefundAmount())
                .refundFee(refund.getRefundFee())
                .refundStatus(refund.getRefundStatus() == null ? null : refund.getRefundStatus().getValue())
                .applyTime(refund.getApplyTime())
                .refundTime(refund.getRefundTime())
                .failReason(refund.getFailReason())
                .items(items)
                .build();
    }

    private <T> T inTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> supplier.get());
    }

    /**
     * 状态字符串常量（避免直接依赖 OrderState 值格式）
     */
    private static final class OrderStateRef {
        private static final String REFUND_APPLY = "920";
    }
}
