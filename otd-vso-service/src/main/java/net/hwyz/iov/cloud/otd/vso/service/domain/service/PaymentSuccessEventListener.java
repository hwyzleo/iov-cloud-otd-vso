package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.event.PaymentSuccessDomainEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 支付成功事件监听：事务提交后取消小订单支付超时任务
 * <p>
 * OTD-VSO-DSN-CR-005：原实现从未被发布（PaymentSuccessDomainEvent 无发布点），
 * 导致已支付订单仍会被 SMALL_ORDER_PAY_TIMEOUT 任务置为失效；同时原监听内重复调用
 * order.pay() 会二次推进订单状态。现改为：
 * <ul>
 *     <li>由 PaymentCallbackService 在支付成功后发布事件；</li>
 *     <li>AFTER_COMMIT 执行（Redis 取消失败不回滚支付事务）；</li>
 *     <li>只取消超时任务，不再推进订单状态。</li>
 * </ul>
 * 状态守卫（INV-008）为正确性保证，本监听为避免无效任务与无效告警的常规修正。
 *
 * @author VSO Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSuccessEventListener {

    static final String SMALL_ORDER_PAY_TIMEOUT_TASK = "SMALL_ORDER_PAY_TIMEOUT";

    private final TimeoutNotifyService timeoutNotifyService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessDomainEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }
        try {
            timeoutNotifyService.cancelByOrderIdAndType(event.getOrderId(), SMALL_ORDER_PAY_TIMEOUT_TASK);
            log.info("支付成功取消超时任务：orderId={}, paymentStage={}",
                    event.getOrderId(), event.getPaymentStage());
        } catch (Exception e) {
            // 取消失败不影响支付结果；状态守卫会跳过非待支付订单的失效处理
            log.warn("支付成功取消超时任务失败：orderId={}, reason={}", event.getOrderId(), e.getMessage());
        }
    }
}
