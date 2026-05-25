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
        
        switch (task.getTriggerStrategy()) {
            case "remind":
                // 仅提醒
                timeoutNotifyService.sendTimeoutReminder(task.getOrderId(), 
                                                        task.getTaskType(), 
                                                        "SYSTEM");
                task.complete();
                break;
                
            case "close":
                log.info("自动关闭订单：orderId={}", task.getOrderId());
                try {
                    orderDomainService.closeOrder(task.getOrderId(), "超时自动关闭");
                    task.complete();
                } catch (Exception e) {
                    log.error("自动关闭订单失败：orderId={}", task.getOrderId(), e);
                    task.fail();
                }
                break;
                
            case "invalid":
                // 自动失效订单（小订单）
                log.info("自动失效小订单：orderId={}", task.getOrderId());
                orderDomainService.invalidateSmallOrder(task.getOrderId());
                task.complete();
                break;
                
            case "retry_and_alert":
                if (task.canRetry()) {
                    log.warn("任务重试：taskId={}, retryCount={}", task.getTaskId(), task.getRetryCount());
                    task.fail();
                } else {
                    log.error("任务重试超过限制，发送告警：taskId={}", task.getTaskId());
                    timeoutNotifyService.sendTimeoutAlert(task.getOrderId(), task.getTaskType(), task.getTaskId());
                    task.complete();
                }
                break;
                
            default:
                log.warn("未知的触发策略：strategy={}", task.getTriggerStrategy());
                task.complete();
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
