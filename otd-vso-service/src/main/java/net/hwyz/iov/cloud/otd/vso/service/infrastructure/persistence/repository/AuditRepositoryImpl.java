package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.AuditRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.CallbackLogMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderVersionMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderTimelineMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderLockMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderLockPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 审计与版本仓储实现
 */
@Repository
@RequiredArgsConstructor
public class AuditRepositoryImpl implements AuditRepository {

    private final CallbackLogMapper callbackLogMapper;
    private final OrderVersionMapper orderVersionMapper;
    private final OrderTimelineMapper orderTimelineMapper;
    private final OrderLockMapper orderLockMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CallbackLogPo saveCallbackLog(CallbackLogPo callbackLogPo) {
        if (callbackLogPo.getId() == null) {
            callbackLogMapper.insert(callbackLogPo);
        } else {
            callbackLogMapper.updateById(callbackLogPo);
        }
        return callbackLogPo;
    }

    @Override
    public Optional<CallbackLogPo> findByIdempotentKey(String idempotentKey) {
        LambdaQueryWrapper<CallbackLogPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CallbackLogPo::getIdempotentKey, idempotentKey)
               .eq(CallbackLogPo::getRowValid, 1)
               .orderByDesc(CallbackLogPo::getProcessTime);
        return Optional.ofNullable(callbackLogMapper.selectOne(wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVersionPo saveVersion(OrderVersionPo versionPo) {
        if (versionPo.getId() == null) {
            orderVersionMapper.insert(versionPo);
        } else {
            orderVersionMapper.updateById(versionPo);
        }
        return versionPo;
    }

    @Override
    public List<OrderVersionPo> findVersionsByOrderId(String orderId) {
        LambdaQueryWrapper<OrderVersionPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderVersionPo::getOrderId, orderId)
               .eq(OrderVersionPo::getRowValid, 1)
               .orderByAsc(OrderVersionPo::getVersionNo);
        return orderVersionMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderTimelinePo saveTimeline(OrderTimelinePo timelinePo) {
        if (timelinePo.getId() == null) {
            orderTimelineMapper.insert(timelinePo);
        } else {
            orderTimelineMapper.updateById(timelinePo);
        }
        return timelinePo;
    }

    @Override
    public List<OrderTimelinePo> findTimelinesByOrderId(String orderId) {
        LambdaQueryWrapper<OrderTimelinePo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderTimelinePo::getOrderId, orderId)
               .eq(OrderTimelinePo::getRowValid, 1)
               .orderByDesc(OrderTimelinePo::getEventTime);
        return orderTimelineMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderLockPo saveLock(OrderLockPo lockPo) {
        if (lockPo.getId() == null) {
            orderLockMapper.insert(lockPo);
        } else {
            orderLockMapper.updateById(lockPo);
        }
        return lockPo;
    }

    @Override
    public Optional<OrderLockPo> findByOrderIdAndScene(String orderId, String lockScene) {
        LambdaQueryWrapper<OrderLockPo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderLockPo::getOrderId, orderId)
               .eq(OrderLockPo::getLockScene, lockScene)
               .eq(OrderLockPo::getRowValid, 1)
               .orderByDesc(OrderLockPo::getLockStartTime);
        return Optional.ofNullable(orderLockMapper.selectOne(wrapper));
    }

}
