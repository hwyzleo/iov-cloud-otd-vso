package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import java.util.Optional;

public interface CallbackLogRepository {
    CallbackLogPo save(CallbackLogPo callbackLogPo);
    Optional<CallbackLogPo> findByIdempotentKey(String idempotentKey);
    Optional<CallbackLogPo> findByExternalBusinessNo(String externalBusinessNo);
}