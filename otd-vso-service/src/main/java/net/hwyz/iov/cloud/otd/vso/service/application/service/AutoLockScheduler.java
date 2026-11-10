package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * 定金支付 48 小时自动锁单补偿扫描（OTD-VSO-DSN-CR-002 US-007）
 * <p>
 * 每分钟扫描数据库中已到期且状态仍为 DOWN_PAYMENT_PAID 的订单；
 * 不依赖 Redis，服务重启后仍可补偿；锁单统一走订单锁与领域方法。
 *
 * @author VSO Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutoLockScheduler {

    private final OrderRepository orderRepository;
    private final AutoLockExecutor autoLockExecutor;

    /** 自动锁单阈值（小时） */
    @Value("${vso.order.auto-lock-hours:48}")
    private int autoLockHours = 48;

    /** 单次扫描上限 */
    @Value("${vso.order.auto-lock-batch:200}")
    private int batchSize = 200;

    /**
     * 每分钟执行一次（固定延迟）
     */
    @Scheduled(fixedDelayString = "${vso.order.auto-lock-interval-ms:60000}", initialDelayString = "${vso.order.auto-lock-initial-delay-ms:60000}")
    public void scan() {
        LocalDateTime deadline = LocalDateTime.now().minusHours(autoLockHours);
        Date deadlineDate = Date.from(deadline.atZone(ZoneId.systemDefault()).toInstant());
        List<Order> candidates = orderRepository.findDownPaymentPaidBefore(deadlineDate, batchSize);
        if (candidates.isEmpty()) {
            return;
        }
        log.info("自动锁单扫描：到期订单数={}", candidates.size());
        for (Order order : candidates) {
            try {
                autoLockExecutor.lockExpiredOrder(order.getOrderNo());
            } catch (Exception e) {
                log.error("自动锁单失败：orderNo={}", order.getOrderNo(), e);
            }
        }
    }
}
