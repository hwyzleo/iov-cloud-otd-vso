package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVersionPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderTimelinePo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderLockPo;

import java.util.List;
import java.util.Optional;

/**
 * 审计与版本仓储接口
 */
public interface AuditRepository {

    CallbackLogPo saveCallbackLog(CallbackLogPo callbackLogPo);

    Optional<CallbackLogPo> findByIdempotentKey(String idempotentKey);

    OrderVersionPo saveVersion(OrderVersionPo versionPo);

    List<OrderVersionPo> findVersionsByOrderId(String orderId);

    OrderTimelinePo saveTimeline(OrderTimelinePo timelinePo);

    List<OrderTimelinePo> findTimelinesByOrderId(String orderId);

    OrderLockPo saveLock(OrderLockPo lockPo);

    Optional<OrderLockPo> findByOrderIdAndScene(String orderId, String lockScene);

}
