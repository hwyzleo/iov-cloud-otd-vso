package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.CallbackLogRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.CallbackLogMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.CallbackLogPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CallbackLogRepositoryImpl implements CallbackLogRepository {
    private final CallbackLogMapper callbackLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CallbackLogPo save(CallbackLogPo callbackLogPo) {
        if (callbackLogPo.getId() == null) {
            callbackLogMapper.insertPo(callbackLogPo);
        } else {
            callbackLogMapper.updatePo(callbackLogPo);
        }
        return callbackLogPo;
    }

    @Override
    public Optional<CallbackLogPo> findByIdempotentKey(String idempotentKey) {
        CallbackLogPo po = callbackLogMapper.selectByIdempotentKey(idempotentKey);
        return Optional.ofNullable(po);
    }

    @Override
    public Optional<CallbackLogPo> findByExternalBusinessNo(String externalBusinessNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("externalBusinessNo", externalBusinessNo);
        params.put("rowValid", 1);
        return callbackLogMapper.selectPoByMap(params).stream().findFirst();
    }
}