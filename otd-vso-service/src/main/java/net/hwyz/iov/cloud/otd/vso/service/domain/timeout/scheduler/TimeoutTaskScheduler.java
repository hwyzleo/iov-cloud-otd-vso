package net.hwyz.iov.cloud.otd.vso.service.domain.timeout.scheduler;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.TimeoutNotifyService;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超时任务调度器
 *
 * @author VSO Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeoutTaskScheduler {

    private final TimeoutNotifyService timeoutNotifyService;
    private final OrderDomainService orderDomainService;
    private final SupplementaryPaymentRepository supplementaryPaymentRepository;
    private final AuditRepository auditRepository;

    /**
     * 每分钟检查一次过期任务
     */
    @Scheduled(fixedRate = 60000)
    public void checkExpiredTasks() {
        log.info("开始检查过期超时任务");
        
        List<TimeoutTask> expiredTasks = timeoutNotifyService.getExpiredTasks();
        
        for (TimeoutTask task : expiredTasks) {
            try {
                handleExpiredTask(task);
            } catch (Exception e) {
                log.error("处理过期任务失败：taskId={}", task.getTaskId(), e);
                task.fail();
            }
        }
        
        log.info("完成检查过期任务，共处理 {} 个任务", expiredTasks.size());
    }

    /**
     * 处理过期任务
     */
    private void handleExpiredTask(TimeoutTask task) {
        log.info("处理过期任务：taskId={}, orderId={}, taskType={}, strategy={}", 
                task.getTaskId(), task.getOrderId(), task.getTaskType(), task.getTriggerStrategy());
        
        task.trigger();
        
        // OTD-VSO-DSN-CR-005：任务必须一次性消费（完成后从本地缓存与 Redis 移除），
        // 原实现只调用 task.complete()，任务仍留在待触发集合中，导致每分钟重复执行。
        switch (task.getTriggerStrategy()) {
            case "remind":
                // 仅提醒
                try {
                    timeoutNotifyService.sendTimeoutReminder(task.getOrderId(),
                                                            task.getTaskType(),
                                                            "SYSTEM");
                    timeoutNotifyService.completeTimeoutTask(task.getTaskId());
                } catch (Exception e) {
                    log.error("发送超时提醒失败：orderId={}", task.getOrderId(), e);
                    timeoutNotifyService.failTimeoutTask(task);
                }
                break;

            case "close":
                log.info("自动关闭订单：orderId={}", task.getOrderId());
                try {
                    orderDomainService.closeOrder(task.getOrderId(), "超时自动关闭");
                    timeoutNotifyService.completeTimeoutTask(task.getTaskId());
                } catch (Exception e) {
                    log.error("自动关闭订单失败：orderId={}", task.getOrderId(), e);
                    timeoutNotifyService.failTimeoutTask(task);
                }
                break;

            case "auto_close":
                log.info("审核驳回超时自动关闭订单：orderId={}", task.getOrderId());
                try {
                    orderDomainService.rejectTimeoutClose(task.getOrderId());
                    timeoutNotifyService.completeTimeoutTask(task.getTaskId());
                } catch (Exception e) {
                    log.error("审核驳回超时关闭订单失败：orderId={}", task.getOrderId(), e);
                    timeoutNotifyService.failTimeoutTask(task);
                }
                break;

            case "invalid":
                // 自动失效订单（小订单）；OTD-VSO-DSN-CR-005：非待支付订单跳过
                log.info("自动失效小订单：orderId={}", task.getOrderId());
                try {
                    boolean invalidated = orderDomainService.invalidateSmallOrder(task.getOrderId());
                    if (!invalidated) {
                        log.info("小订单非待支付状态，跳过失效：orderId={}", task.getOrderId());
                    }
                    timeoutNotifyService.completeTimeoutTask(task.getTaskId());
                } catch (Exception e) {
                    log.error("自动失效小订单失败：orderId={}", task.getOrderId(), e);
                    timeoutNotifyService.failTimeoutTask(task);
                }
                break;

            case "retry_and_alert":
                if (task.canRetry()) {
                    log.warn("任务重试：taskId={}, retryCount={}", task.getTaskId(), task.getRetryCount());
                    timeoutNotifyService.failTimeoutTask(task);
                } else {
                    log.error("任务重试超过限制，发送告警：taskId={}", task.getTaskId());
                    timeoutNotifyService.sendTimeoutAlert(task.getOrderId(), task.getTaskType(), task.getTaskId());
                    timeoutNotifyService.completeTimeoutTask(task.getTaskId());
                }
                break;

            default:
                log.warn("未知的触发策略：strategy={}", task.getTriggerStrategy());
                timeoutNotifyService.completeTimeoutTask(task.getTaskId());
        }
    }

    /**
     * 扫描过期的补款任务
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void scanExpiredSupplementaryPayments() {
        LocalDateTime now = LocalDateTime.now();
        List<SupplementaryPaymentPo> expiredTasks = supplementaryPaymentRepository.findExpiredPendingTasks(now);

        for (SupplementaryPaymentPo task : expiredTasks) {
            try {
                supplementaryPaymentRepository.updateStatus(task.getSupplementaryNo(), SupplementaryPaymentStatus.EXPIRED, null);
                log.info("补款任务过期自动取消: supplementaryNo={}", task.getSupplementaryNo());

                saveOrderTimeline(task.getOrderId(), "SUPPLEMENT_EXPIRED", "补款任务过期",
                        String.format("补款单号: %s, 金额: %s", task.getSupplementaryNo(), task.getSupplementaryAmount()));
            } catch (Exception e) {
                log.error("处理过期补款任务异常: supplementaryNo={}", task.getSupplementaryNo(), e);
            }
        }
    }

    private void saveOrderTimeline(String orderId, String eventType, String eventName, String eventRemark) {
        OrderTimelinePo timelinePo = new OrderTimelinePo();
        timelinePo.setTimelineId(IdUtil.fastSimpleUUID());
        timelinePo.setOrderId(orderId);
        timelinePo.setEventType(eventType);
        timelinePo.setEventName(eventName);
        timelinePo.setEventRemark(eventRemark);
        timelinePo.setEventTime(LocalDateTime.now());
        auditRepository.saveTimeline(timelinePo);
        log.info("保存订单时间线：orderId={}, eventType={}", orderId, eventType);
    }

}
