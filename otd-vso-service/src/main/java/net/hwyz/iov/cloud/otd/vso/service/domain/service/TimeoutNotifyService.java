package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.TimeoutTask;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.NotifyTask;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 超时与通知服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutNotifyService {

    private static final String TIMEOUT_TASK_PREFIX = "vso:timeout:task:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 待触发的超时任务（本地缓存，主存储为 Redis）
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
        
        // 保存到本地缓存
        pendingTasks.put(taskId, task);
        
        // 保存到 Redis，TTL 为阈值 + 5 分钟
        saveToRedis(task, thresholdMinutes + 5);
        
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
            deleteFromRedis(taskId);
        }
    }

    /**
     * 根据订单ID和任务类型取消超时任务
     *
     * @param orderId 订单 ID
     * @param taskType 任务类型
     */
    public void cancelByOrderIdAndType(String orderId, String taskType) {
        pendingTasks.values().stream()
                .filter(task -> task.getOrderId().equals(orderId) && task.getTaskType().equals(taskType))
                .forEach(task -> {
                    task.cancel();
                    pendingTasks.remove(task.getTaskId());
                    deleteFromRedis(task.getTaskId());
                });
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
            deleteFromRedis(taskId);
        }
    }

    /**
     * 获取已过期的超时任务
     *
     * @return 过期的任务列表
     */
    public List<TimeoutTask> getExpiredTasks() {
        List<TimeoutTask> expired = new ArrayList<>();
        
        // 先从本地缓存获取
        for (TimeoutTask task : pendingTasks.values()) {
            if (task.isExpired()) {
                expired.add(task);
            }
        }
        
        // 从 Redis 获取（处理服务重启场景）
        try {
            Set<String> keys = redisTemplate.keys(TIMEOUT_TASK_PREFIX + "*");
            if (keys != null) {
                for (String key : keys) {
                    String taskId = key.replace(TIMEOUT_TASK_PREFIX, "");
                    // 跳过已在本地缓存中的任务
                    if (pendingTasks.containsKey(taskId)) {
                        continue;
                    }
                    TimeoutTask task = loadFromRedis(taskId);
                    if (task != null && task.isExpired()) {
                        expired.add(task);
                        // 加入本地缓存
                        pendingTasks.put(taskId, task);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从 Redis 获取超时任务失败，降级使用本地缓存", e);
        }
        
        return expired;
    }

    /**
     * 保存超时任务到 Redis
     *
     * @param task 超时任务
     * @param ttlMinutes TTL（分钟）
     */
    private void saveToRedis(TimeoutTask task, int ttlMinutes) {
        try {
            String key = TIMEOUT_TASK_PREFIX + task.getTaskId();
            String json = objectMapper.writeValueAsString(task);
            redisTemplate.opsForValue().set(key, json, ttlMinutes, TimeUnit.MINUTES);
            log.debug("超时任务已保存到 Redis: taskId={}", task.getTaskId());
        } catch (JsonProcessingException e) {
            log.warn("序列化超时任务失败: taskId={}", task.getTaskId(), e);
        } catch (Exception e) {
            log.warn("保存超时任务到 Redis 失败，降级使用本地缓存: taskId={}", task.getTaskId(), e);
        }
    }

    /**
     * 从 Redis 加载超时任务
     *
     * @param taskId 任务 ID
     * @return 超时任务，不存在则返回 null
     */
    private TimeoutTask loadFromRedis(String taskId) {
        try {
            String key = TIMEOUT_TASK_PREFIX + taskId;
            String json = redisTemplate.opsForValue().get(key);
            if (json != null) {
                return objectMapper.readValue(json, TimeoutTask.class);
            }
        } catch (JsonProcessingException e) {
            log.warn("反序列化超时任务失败: taskId={}", taskId, e);
        } catch (Exception e) {
            log.warn("从 Redis 加载超时任务失败: taskId={}", taskId, e);
        }
        return null;
    }

    /**
     * 从 Redis 删除超时任务
     *
     * @param taskId 任务 ID
     */
    private void deleteFromRedis(String taskId) {
        try {
            String key = TIMEOUT_TASK_PREFIX + taskId;
            redisTemplate.delete(key);
            log.debug("超时任务已从 Redis 删除: taskId={}", taskId);
        } catch (Exception e) {
            log.warn("从 Redis 删除超时任务失败: taskId={}", taskId, e);
        }
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

    /**
     * 发送超时任务告警
     *
     * @param orderId 订单 ID
     * @param timeoutType 超时类型
     * @param taskId 任务 ID
     */
    public void sendTimeoutAlert(String orderId, String timeoutType, String taskId) {
        String notifyType = "TIMEOUT_ALERT";
        Map<String, Object> params = new ConcurrentHashMap<>();
        params.put("orderId", orderId);
        params.put("timeoutType", timeoutType);
        params.put("taskId", taskId);
        params.put("retryExceeded", true);
        params.put("time", LocalDateTime.now());
        
        createNotifyTask(orderId, notifyType, "system", "SYSTEM_ADMIN", 
                        "TIMEOUT_ALERT", params);
        log.error("超时任务告警：orderId={}, timeoutType={}, taskId={}", orderId, timeoutType, taskId);
    }

}
