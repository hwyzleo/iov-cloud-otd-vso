package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStage;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiatePaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.InitiatePaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderAmountSnapshotMissingException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderExpiredException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentChannelNotAvailableException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.config.PaymentChannelConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 支付应用服务
 * <p>
 * US-004 / INV-002 / INV-004 / INV-005 / INV-006：
 * 支付金额取订单金额快照；订单锁内返回已有活动支付单；
 * TX1 创建 pending → 提交后调用 Mock → TX2 保存外部参数。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentAppService {

    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final PaymentRepository paymentRepository;
    private final OrderLockService orderLockService;
    private final PaymentAdapter paymentAdapter;
    private final PaymentChannelConfig paymentChannelConfig;
    private final PlatformTransactionManager transactionManager;
    private final MockPaymentAutoCallbackService mockPaymentAutoCallbackService;

    /**
     * 发起支付
     */
    public InitiatePaymentResult initiatePayment(InitiatePaymentCmd cmd) {
        if (cmd.getPaymentChannel() == null || !paymentChannelConfig.isChannelEnabled(cmd.getPaymentChannel())) {
            throw new PaymentChannelNotAvailableException(String.valueOf(cmd.getPaymentChannel()));
        }

        // 账号订单查询 → 金额加载 → 状态校验
        Order order = loadOrderSnapshot(cmd.getAccountId(), cmd.getOrderNo());
        PaymentStage stage = resolvePaymentStage(order);
        BigDecimal amount = resolvePaymentAmount(order, stage);

        // 订单锁内：再次校验 → 有效单返回 / 新建
        Payment payment = orderLockService.executeWithLock(order.getId(), cmd.getAccountId(), "payment",
                () -> obtainActiveOrNewPayment(order, stage, amount, cmd.getAccountId(), cmd.getPaymentChannel()));

        if (payment.getExternalTradeNo() != null) {
            // 复用已有活动支付单，不重复调用网关。
            // 注意：仍然要尝试 mock 自回调——否则「上一次发起支付时自回调未生效」留下的
            // PROCESSING 支付单会被一直复用，客户端永远等不到终态（真实缺陷，2026-11-13）。
            maybeAutoCallback(payment.getPaymentNo());
            return toInitiateResult(payment, cmd.getPaymentChannel(), null, null);
        }

        // 本地初始事务已提交，调用 Mock 网关
        PaymentGatewayResult gatewayResult;
        try {
            gatewayResult = paymentAdapter.createPayment(payment.getPaymentNo(), amount, cmd.getPaymentChannel().name());
        } catch (Exception e) {
            log.error("支付网关调用失败：paymentNo={}", payment.getPaymentNo(), e);
            markGatewayFailed(payment.getPaymentNo(), "外部支付网关调用失败：" + e.getMessage());
            throw new PaymentConflictException("支付网关调用失败，支付单[" + payment.getPaymentNo() + "]已标记失败");
        }

        // TX2：保存外部参数
        Payment updated = inTransaction(() -> {
            Payment current = paymentRepository.findByPaymentNo(payment.getPaymentNo())
                    .orElseThrow(() -> new PaymentNotExistException(payment.getPaymentNo()));
            current.markProcessing(gatewayResult.getExternalTradeNo());
            paymentRepository.save(current);
            return current;
        });

        maybeAutoCallback(updated.getPaymentNo());

        return toInitiateResult(updated, cmd.getPaymentChannel(),
                gatewayResult.getPaymentMerchant(), gatewayResult.getPaymentReference());
    }

    /**
     * mock 模式下由服务端扮演支付渠道触发签名回调（OTD-VSO-DSN-CR-007）：
     * 客户端只轮询、不持密钥；失败仅告警，不影响发起支付结果。
     */
    private void maybeAutoCallback(String paymentNo) {
        try {
            mockPaymentAutoCallbackService.triggerIfEnabled(paymentNo);
        } catch (Exception e) {
            log.warn("Mock 自动回调失败（不影响发起支付）: paymentNo={}", paymentNo, e);
        }
    }

    /**
     * 查询支付单
     */
    public PaymentResult getPayment(String accountId, String orderNo, String paymentNo) {
        Payment payment = paymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new PaymentNotExistException(paymentNo));
        Order order = orderRepository.findByOrderIdAndAccountId(payment.getOrderId(), accountId)
                .orElseThrow(() -> new OrderNotExistException(orderNo == null ? payment.getOrderId() : orderNo));
        if (orderNo != null && order.getOrderNo() != null && !order.getOrderNo().equals(orderNo)) {
            throw new OrderNotExistException(orderNo);
        }
        // 本地已有终态时返回本地事实；活动状态查询网关（Mock 默认 PROCESSING，不改变本地状态）
        return toPaymentResult(payment, order);
    }

    /**
     * 查询订单支付记录（创建时间倒序）
     */
    public List<PaymentResult> listPayments(String accountId, String orderNo) {
        Order order = loadOrderSnapshot(accountId, orderNo);
        return toPaymentResults(order);
    }

    /**
     * 运营（MPT）按业务订单 ID 查询支付记录（只读，不做账号隔离）
     */
    public List<PaymentResult> listPaymentsByOrderId(String orderId) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new OrderNotExistException(orderId));
        return toPaymentResults(order);
    }

    private List<PaymentResult> toPaymentResults(Order order) {
        return paymentRepository.findByOrderId(order.getId()).stream()
                .map(payment -> toPaymentResult(payment, order))
                .collect(Collectors.toList());
    }

    /**
     * 支付成功后累计已支付金额（按 paymentId 幂等）
     */
    public void accumulatePaidAmount(Order order, BigDecimal amount) {
        OrderAmount orderAmount = loadOrderAmount(order);
        orderAmount.addPaidAmount(new net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money(amount));
        order.setOrderAmount(orderAmount);
    }

    /**
     * 加载订单金额快照，缺失时拒绝（禁止按当前价格补值）
     */
    public OrderAmount loadOrderAmount(Order order) {
        return orderAmountRepository.findDomainByOrderId(order.getId())
                .orElseThrow(() -> new OrderAmountSnapshotMissingException(
                        "订单[" + order.getOrderNo() + "]金额快照缺失，需人工修复"));
    }

    private Order loadOrderSnapshot(String accountId, String orderNo) {
        Order order = orderRepository.findByOrderNoAndAccountId(orderNo, accountId)
                .orElseThrow(() -> new OrderNotExistException(orderNo));
        OrderAmount amount = loadOrderAmount(order);
        order.setOrderAmount(amount);
        return order;
    }

    private PaymentStage resolvePaymentStage(Order order) {
        OrderState state = order.getOrderState();
        if (state == OrderState.EARNEST_MONEY_UNPAID) {
            return PaymentStage.EARNEST_MONEY;
        }
        if (state == OrderState.DOWN_PAYMENT_UNPAID) {
            return PaymentStage.DOWN_PAYMENT;
        }
        // OTD-VSO-DSN-CR-005：已失效订单返回可识别错误码，供客户端引导重新支付
        if (state == OrderState.EXPIRED) {
            throw new OrderExpiredException(
                    "车辆销售订单[" + order.getOrderNo() + "]已失效，请重新发起支付");
        }
        throw new OrderStateNotAllowedException(
                "车辆销售订单[" + order.getOrderNo() + "]当前状态[" + state + "]不允许发起支付");
    }

    /**
     * 支付金额必须取自订单金额快照，禁止取车型最低价
     */
    private BigDecimal resolvePaymentAmount(Order order, PaymentStage stage) {
        OrderAmount amount = order.getOrderAmount();
        BigDecimal payable = stage == PaymentStage.EARNEST_MONEY
                ? amount.getDepositAmount().getAmount()
                : amount.getDownPaymentAmount().getAmount();
        if (payable == null || payable.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OrderAmountSnapshotMissingException(
                    "订单[" + order.getOrderNo() + "]阶段[" + stage + "]应付金额缺失，需人工修复");
        }
        return payable;
    }

    private Payment obtainActiveOrNewPayment(Order order, PaymentStage stage, BigDecimal amount,
                                             String accountId, PaymentChannel channel) {
        Optional<Payment> active = paymentRepository.findActiveByOrderIdAndStage(order.getId(), stage.name());
        LocalDateTime now = LocalDateTime.now();
        int staleMinutes = paymentChannelConfig.processingStaleMinutesOrDefault();
        if (active.isPresent()) {
            Payment current = active.get();
            // OTD-VSO-DSN-CR-005：PROCESSING 超过 expire_time + 宽限期视为失联，关闭后允许新建
            if (current.closeIfStale(now, staleMinutes)) {
                paymentRepository.save(current);
                log.info("处理中支付单失联关闭：paymentNo={}, expireTime={}",
                        current.getPaymentNo(), current.getExpireTime());
                return createPendingPayment(order, stage, amount, accountId, channel);
            }
            if (current.isActive(now)) {
                return current;
            }
            if (current.isExpired(now)) {
                current.closeExpired();
                paymentRepository.save(current);
                log.info("活动支付单已过期关闭：paymentNo={}", current.getPaymentNo());
            }
        }
        return createPendingPayment(order, stage, amount, accountId, channel);
    }

    private Payment createPendingPayment(Order order, PaymentStage stage, BigDecimal amount,
                                         String accountId, PaymentChannel channel) {
        int timeoutMinutes = stage == PaymentStage.EARNEST_MONEY
                ? paymentChannelConfig.getSmallOrderTimeoutMinutes()
                : paymentChannelConfig.getDownPaymentTimeoutMinutes();
        Payment payment = Payment.builder()
                .paymentId(IdUtil.nanoId(15))
                .paymentNo("P" + IdUtil.nanoId(12))
                .orderId(order.getId())
                .paymentStage(stage.name())
                .paymentChannel(channel.name())
                .paymentAmount(amount)
                .paymentStatus(PaymentStatus.PENDING_PAYMENT)
                .initiatorRole("capp_user")
                .initiatorId(accountId)
                .expireTime(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();
        return inTransaction(() -> paymentRepository.save(payment));
    }

    private void markGatewayFailed(String paymentNo, String reason) {
        inTransaction(() -> {
            Payment current = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
            if (current == null) {
                return null;
            }
            try {
                current.markFailed(reason);
                paymentRepository.save(current);
            } catch (Exception ex) {
                log.warn("标记支付失败被拒绝：paymentNo={}, reason={}", paymentNo, ex.getMessage());
            }
            return current;
        });
    }

    private <T> T inTransaction(java.util.function.Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        return template.execute(status -> supplier.get());
    }

    private InitiatePaymentResult toInitiateResult(Payment payment, PaymentChannel channel,
                                                   String merchant, String reference) {
        return InitiatePaymentResult.builder()
                .paymentNo(payment.getPaymentNo())
                .paymentChannel(channel)
                .paymentAmount(payment.getPaymentAmount())
                .paymentStatus(payment.getPaymentStatus() == null ? null : payment.getPaymentStatus().name())
                .externalTradeNo(payment.getExternalTradeNo())
                .expireTime(payment.getExpireTime())
                .paymentMerchant(merchant)
                .paymentReference(reference)
                .build();
    }

    private PaymentResult toPaymentResult(Payment payment, Order order) {
        return PaymentResult.builder()
                .paymentNo(payment.getPaymentNo())
                .orderNo(order.getOrderNo())
                .paymentStage(payment.getPaymentStage())
                .paymentChannel(payment.getPaymentChannel())
                .paymentAmount(payment.getPaymentAmount())
                .paymentStatus(payment.getPaymentStatus() == null ? null : payment.getPaymentStatus().name())
                .externalTradeNo(payment.getExternalTradeNo())
                .payTime(payment.getPayTime())
                .expireTime(payment.getExpireTime())
                .failReason(payment.getFailReason())
                .build();
    }
}
