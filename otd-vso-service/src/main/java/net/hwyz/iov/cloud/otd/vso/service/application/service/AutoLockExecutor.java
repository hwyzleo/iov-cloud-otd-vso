package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderTimelineEventType;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderLockService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 自动锁单执行器
 * <p>
 * 与手动锁单共用订单锁与领域方法；到期订单只允许 DOWN_PAYMENT_PAID → ARRANGE_PRODUCTION。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AutoLockExecutor {

    private static final long AUTO_LOCK_HOURS = 48L;

    private final OrderRepository orderRepository;
    private final OrderAmountRepository orderAmountRepository;
    private final OrderLockService orderLockService;
    private final OrderTimelineAppService orderTimelineAppService;

    /**
     * 执行到期自动锁单
     */
    public void lockExpiredOrder(String orderNo) {
        Order order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return;
        }
        if (!reachedDeadline(order)) {
            log.debug("订单未达到 48 小时自动锁单阈值：orderNo={}", orderNo);
            return;
        }
        orderLockService.executeWithLock(order.getId(), "SYSTEM_AUTO_LOCK", "autoLock", () -> {
            Order current = orderRepository.findByOrderNo(orderNo).orElse(null);
            if (current == null) {
                return null;
            }
            if (current.getOrderState() == OrderState.ARRANGE_PRODUCTION) {
                log.debug("订单已锁单，自动锁单幂等：orderNo={}", orderNo);
                return null;
            }
            if (current.getOrderState() != OrderState.DOWN_PAYMENT_PAID || !reachedDeadline(current)) {
                log.info("订单状态不满足自动锁单条件，跳过：orderNo={}, state={}", orderNo, current.getOrderState());
                return null;
            }
            orderAmountRepository.findDomainByOrderId(current.getId()).ifPresent(current::setOrderAmount);
            String before = String.valueOf(current.getOrderState().getValue());
            current.lock();
            orderRepository.save(current);
            orderTimelineAppService.record(current.getId(), OrderTimelineEventType.ORDER_LOCKED,
                    before, String.valueOf(current.getOrderState().getValue()),
                    "system", "system", "auto_lock", null, null,
                    "定金支付满 48 小时自动锁单");
            log.info("自动锁单成功：orderNo={}", orderNo);
            return null;
        });
    }

    private boolean reachedDeadline(Order order) {
        if (order.getDownPaymentTime() == null) {
            return false;
        }
        LocalDateTime paidAt = LocalDateTime.ofInstant(order.getDownPaymentTime().toInstant(), ZoneId.systemDefault());
        return !paidAt.plusHours(AUTO_LOCK_HOURS).isAfter(LocalDateTime.now());
    }
}
