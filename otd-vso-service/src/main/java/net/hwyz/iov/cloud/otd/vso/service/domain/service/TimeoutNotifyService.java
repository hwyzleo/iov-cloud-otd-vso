package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.NotifyTask;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 超时与通知服务
 *
 * @author VSO Team
 */
@Service
@RequiredArgsConstructor
public class TimeoutNotifyService {

    /**
     * 待触发的超时任务（内存缓存，生产环境应使用 Redis/DB）
     */
    private final Map<String, TimeoutTask> pendingTasks = new ConcurrentHashMap<>();

    /**
     * 待发送的通知任务
     */
    private final Map<String, NotifyTask> pendingNotifies = new ConcurrentHashMap<>();

    /**
     * 创建超时任务
     *
     * @param orderId 订单 ID
     * @param taskType 任务类型
     * @param triggerStrategy 触发策略
     * @param thresholdMinutes 超时阈值（分钟）
     * @return 超时任务 ID
     */
    public String createTimeoutTask(String orderId, String taskType, 
                                   String triggerStrategy, Integer thresholdMinutes) {
        String taskId = "TK" + System.currentTimeMillis();
        TimeoutTask task = new TimeoutTask(taskId, orderId, taskType, triggerStrategy, thresholdMinutes);
        pendingTasks.put(taskId, task);
        return taskId;
    }

    /**
     * 取消超时任务
     *
     * @param taskId 任务 ID
     */
    public void cancelTimeoutTask(String taskId) {
        TimeoutTask task = pendingTasks.get(taskId);
        if (task != null) {
            task.cancel();
            pendingTasks.remove(taskId);
        }
    }

    /**
     * 完成超时任务
     *
     * @param taskId 任务 ID
     */
    public void completeTimeoutTask(String taskId) {
        TimeoutTask task = pendingTasks.get(taskId);
        if (task != null) {
            task.complete();
            pendingTasks.remove(taskId);
        }
    }

    /**
     * 获取已过期的超时任务
     *
     * @return 过期的任务列表
     */
    public List<TimeoutTask> getExpiredTasks() {
        List<TimeoutTask> expired = new ArrayList<>();
        for (TimeoutTask task : pendingTasks.values()) {
            if (task.isExpired()) {
                expired.add(task);
            }
        }
        return expired;
    }

    /**
     * 创建通知任务
     *
     * @param orderId 订单 ID
     * @param notifyType 通知类型
     * @param receiverType 接收人类型
     * @param receiverId 接收人 ID
     * @param templateCode 模板编码
     * @param params 模板参数
     * @return 通知任务 ID
     */
    public String createNotifyTask(String orderId, String notifyType,
                                   String receiverType, String receiverId,
                                   String templateCode, Map<String, Object> params) {
        String notifyId = "NT" + System.currentTimeMillis();
        NotifyTask notify = new NotifyTask(notifyId, orderId, notifyType, receiverType, receiverId);
        notify.setTemplateCode(templateCode);
        notify.setTemplateParams(params);
        pendingNotifies.put(notifyId, notify);
        return notifyId;
    }

    /**
     * 发送通知
     *
     * @param notifyId 通知 ID
     */
    public void sendNotify(String notifyId) {
        NotifyTask notify = pendingNotifies.get(notifyId);
        if (notify != null) {
            // TODO: 调用实际的通知发送服务（短信、邮件、Push 等）
            notify.send();
            pendingNotifies.remove(notifyId);
        }
    }

    /**
     * 创建并发送订单超时提醒
     *
     * @param orderId 订单 ID
     * @param timeoutType 超时类型
     * @param receiverId 接收人 ID
     */
    public void sendTimeoutReminder(String orderId, String timeoutType, String receiverId) {
        String notifyType = "TIMEOUT_REMINDER";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("orderId", orderId);
        params.put("timeoutType", timeoutType);
        params.put("time", LocalDateTime.now());
        
        createNotifyTask(orderId, notifyType, "customer", receiverId, 
                        "TIMEOUT_REMINDER", params);
    }

    /**
     * 创建并发送订单状态变更通知
     *
     * @param orderId 订单 ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param receiverId 接收人 ID
     */
    public void sendStatusChangeNotify(String orderId, String oldStatus, 
                                       String newStatus, String receiverId) {
        String notifyType = "STATUS_CHANGE";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("orderId", orderId);
        params.put("oldStatus", oldStatus);
        params.put("newStatus", newStatus);
        params.put("time", LocalDateTime.now());
        
        createNotifyTask(orderId, notifyType, "customer", receiverId, 
                        "STATUS_CHANGE", params);
    }

}
