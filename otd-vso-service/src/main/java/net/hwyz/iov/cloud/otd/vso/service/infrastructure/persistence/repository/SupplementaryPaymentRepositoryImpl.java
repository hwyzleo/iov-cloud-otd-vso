package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.SupplementaryPaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SupplementaryPaymentRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.SupplementaryPaymentMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SupplementaryPaymentPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 改配补款仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SupplementaryPaymentRepositoryImpl implements SupplementaryPaymentRepository {

    private final SupplementaryPaymentMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SupplementaryPaymentPo po) {
        if (po.getId() == null) {
            mapper.insertPo(po);
            log.debug("新增补款记录: {}", po.getSupplementaryNo());
        } else {
            mapper.updatePo(po);
            log.debug("更新补款记录: {}", po.getSupplementaryNo());
        }
    }

    @Override
    public Optional<SupplementaryPaymentPo> findBySupplementaryNo(String supplementaryNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("supplementaryNo", supplementaryNo);
        return mapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public List<SupplementaryPaymentPo> findByOrderIdAndStatus(String orderId, SupplementaryPaymentStatus status) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("supplementaryStatus", status.getValue());
        return mapper.selectPoByMap(params);
    }

    @Override
    public List<SupplementaryPaymentPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("orderBy", "create_time DESC");
        return mapper.selectPoByMap(params);
    }

    @Override
    public List<SupplementaryPaymentPo> findExpiredPendingTasks(LocalDateTime expireTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("supplementaryStatus", SupplementaryPaymentStatus.PENDING.getValue());
        params.put("expireTimeLessThan", expireTime);
        return mapper.selectPoByMap(params);
    }

    @Override
    public Optional<SupplementaryPaymentPo> findByPaymentId(String paymentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("paymentId", paymentId);
        return mapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String supplementaryNo, SupplementaryPaymentStatus status, String paymentId) {
        Optional<SupplementaryPaymentPo> opt = findBySupplementaryNo(supplementaryNo);
        if (opt.isPresent()) {
            SupplementaryPaymentPo po = opt.get();
            po.setSupplementaryStatus(status.getValue());
            if (paymentId != null) {
                po.setPaymentId(paymentId);
            }
            mapper.updatePo(po);
        }
    }
}
