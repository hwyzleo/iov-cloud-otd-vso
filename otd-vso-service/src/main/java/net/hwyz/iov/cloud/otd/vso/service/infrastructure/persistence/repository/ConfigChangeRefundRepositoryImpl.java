package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.ConfigChangeRefundStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ConfigChangeRefundRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ConfigChangeRefundMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ConfigChangeRefundPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 改配退款仓储实现
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ConfigChangeRefundRepositoryImpl implements ConfigChangeRefundRepository {

    private final ConfigChangeRefundMapper mapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(ConfigChangeRefundPo po) {
        if (po.getId() == null) {
            mapper.insertPo(po);
            log.debug("新增改配退款记录: {}", po.getRefundTaskNo());
        } else {
            mapper.updatePo(po);
            log.debug("更新改配退款记录: {}", po.getRefundTaskNo());
        }
    }

    @Override
    public Optional<ConfigChangeRefundPo> findByRefundTaskNo(String refundTaskNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("refundTaskNo", refundTaskNo);
        return mapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public List<ConfigChangeRefundPo> findByOrderIdAndStatus(String orderId, ConfigChangeRefundStatus status) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("refundStatus", status.getValue());
        return mapper.selectPoByMap(params);
    }

    @Override
    public List<ConfigChangeRefundPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("orderBy", "create_time DESC");
        return mapper.selectPoByMap(params);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String refundTaskNo, ConfigChangeRefundStatus status, String refundId, String failReason) {
        Optional<ConfigChangeRefundPo> opt = findByRefundTaskNo(refundTaskNo);
        if (opt.isPresent()) {
            ConfigChangeRefundPo po = opt.get();
            po.setRefundStatus(status.getValue());
            if (refundId != null) {
                po.setRefundId(refundId);
            }
            if (failReason != null) {
                po.setFailReason(failReason);
            }
            mapper.updatePo(po);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManualAuditStatus(String refundTaskNo, String auditStatus) {
        Optional<ConfigChangeRefundPo> opt = findByRefundTaskNo(refundTaskNo);
        if (opt.isPresent()) {
            ConfigChangeRefundPo po = opt.get();
            po.setManualAuditStatus(auditStatus);
            mapper.updatePo(po);
        }
    }
}
