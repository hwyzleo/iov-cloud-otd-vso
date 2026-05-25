package net.hwyz.iov.cloud.otd.vso.service.domain.service;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.BindConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.LockConflictException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.PaymentConflictException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 订单锁服务（基于 Redis 的分布式锁）
 *
 * @author VSO Team
 */
@Service
@RequiredArgsConstructor
public class OrderLockService {

    private final StringRedisTemplate redisTemplate;
    private final OrderMapper orderMapper;

    private static final String LOCK_PREFIX = "order:lock:";
    private static final long DEFAULT_EXPIRE_SECONDS = 30;

    /**
     * 尝试获取订单锁
     *
     * @param orderId 订单业务 ID
     * @param operatorId 操作人 ID
     * @param lockScene 锁定场景
     * @return 是否获取成功
     */
    public boolean tryLock(String orderId, String operatorId, String lockScene) {
        String lockKey = LOCK_PREFIX + orderId;
        String lockValue = operatorId + ":" + lockScene + ":" + System.currentTimeMillis();
        
        // 尝试获取分布式锁
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, lockValue, DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        return Boolean.TRUE.equals(acquired);
    }

    /**
     * 释放订单锁
     *
     * @param orderId 订单业务 ID
     * @param operatorId 操作人 ID
     */
    public void unlock(String orderId, String operatorId) {
        String lockKey = LOCK_PREFIX + orderId;
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        
        // 只有持锁人才能释放锁
        if (currentValue != null && currentValue.startsWith(operatorId)) {
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 检查订单是否被锁定
     *
     * @param orderId 订单业务 ID
     * @return 是否锁定
     */
    public boolean isLocked(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    /**
     * 获取锁持有者信息
     *
     * @param orderId 订单业务 ID
     * @return 锁持有者信息，格式：operatorId:lockScene:timestamp
     */
    public String getLockHolder(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        return redisTemplate.opsForValue().get(lockKey);
    }

    /**
     * 强制释放订单锁（仅管理员或特殊场景使用）
     *
     * @param orderId 订单业务 ID
     */
    public void forceUnlock(String orderId) {
        String lockKey = LOCK_PREFIX + orderId;
        redisTemplate.delete(lockKey);
    }

    /**
     * 续期订单锁
     *
     * @param orderId 订单业务 ID
     * @param operatorId 操作人 ID
     * @param expireSeconds 新的过期时间（秒）
     * @return 是否续期成功
     */
    public boolean renewLock(String orderId, String operatorId, long expireSeconds) {
        String lockKey = LOCK_PREFIX + orderId;
        String currentValue = redisTemplate.opsForValue().get(lockKey);
        
        // 只有持锁人才能续期
        if (currentValue != null && currentValue.startsWith(operatorId)) {
            redisTemplate.expire(lockKey, expireSeconds, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 执行加锁操作（带自动释放）
     *
     * @param orderId 订单业务 ID
     * @param operatorId 操作人 ID
     * @param lockScene 锁定场景
     * @param action 加锁期间执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public <T> T executeWithLock(String orderId, String operatorId, String lockScene,
                                  java.util.function.Supplier<T> action) {
        if (!tryLock(orderId, operatorId, lockScene)) {
            throw createConflictException(lockScene);
        }

        try {
            return action.get();
        } finally {
            unlock(orderId, operatorId);
        }
    }

    private RuntimeException createConflictException(String lockScene) {
        return switch (lockScene) {
            case "payment" -> new PaymentConflictException("订单正在支付中，请稍后再试");
            case "bindVehicle" -> new BindConflictException("订单正在绑定车辆中，请稍后再试");
            default -> new LockConflictException("订单正在处理中，请稍后再试");
        };
    }

    /**
     * 执行加锁操作（无返回值）
     */
    public void executeWithLock(String orderId, String operatorId, String lockScene, 
                                Runnable action) {
        executeWithLock(orderId, operatorId, lockScene, () -> {
            action.run();
            return null;
        });
    }

}
