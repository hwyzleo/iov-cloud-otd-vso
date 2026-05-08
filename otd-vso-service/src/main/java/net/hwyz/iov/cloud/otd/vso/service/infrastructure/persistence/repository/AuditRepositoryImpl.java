package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
            callbackLogMapper.insertPo(callbackLogPo);
        } else {
            callbackLogMapper.updatePo(callbackLogPo);
        }
        return callbackLogPo;
    }

    @Override
    public Optional<CallbackLogPo> findByIdempotentKey(String idempotentKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("idempotentKey", idempotentKey);
        params.put("rowValid", 1);
        params.put("orderBy", "processTime DESC");
        return callbackLogMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVersionPo saveVersion(OrderVersionPo versionPo) {
        if (versionPo.getId() == null) {
            orderVersionMapper.insertPo(versionPo);
        } else {
            orderVersionMapper.updatePo(versionPo);
        }
        return versionPo;
    }

    @Override
    public List<OrderVersionPo> findVersionsByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "versionNo ASC");
        return orderVersionMapper.selectPoByMap(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderTimelinePo saveTimeline(OrderTimelinePo timelinePo) {
        if (timelinePo.getId() == null) {
            orderTimelineMapper.insertPo(timelinePo);
        } else {
            orderTimelineMapper.updatePo(timelinePo);
        }
        return timelinePo;
    }

    @Override
    public List<OrderTimelinePo> findTimelinesByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "eventTime DESC");
        return orderTimelineMapper.selectPoByMap(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderLockPo saveLock(OrderLockPo lockPo) {
        if (lockPo.getId() == null) {
            orderLockMapper.insertPo(lockPo);
        } else {
            orderLockMapper.updatePo(lockPo);
        }
        return lockPo;
    }

    @Override
    public Optional<OrderLockPo> findByOrderIdAndScene(String orderId, String lockScene) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("lockScene", lockScene);
        params.put("rowValid", 1);
        params.put("orderBy", "lockStartTime DESC");
        return orderLockMapper.selectPoByMap(params).stream().findFirst();
    }

}