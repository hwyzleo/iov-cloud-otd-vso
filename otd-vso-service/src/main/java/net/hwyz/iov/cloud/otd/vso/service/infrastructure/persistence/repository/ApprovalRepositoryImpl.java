package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ApprovalRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.ApprovalMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ApprovalRepositoryImpl implements ApprovalRepository {

    private final ApprovalMapper approvalMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalPo save(ApprovalPo approvalPo) {
        if (approvalPo.getId() == null) {
            approvalMapper.insertPo(approvalPo);
        } else {
            approvalMapper.updatePo(approvalPo);
        }
        return approvalPo;
    }

    @Override
    public Optional<ApprovalPo> findByApprovalNo(String approvalNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("approvalNo", approvalNo);
        params.put("rowValid", 1);
        return approvalMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    public Optional<ApprovalPo> findByOrderId(String orderId) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("rowValid", 1);
        params.put("orderBy", "submitTime DESC");
        return approvalMapper.selectPoByMap(params).stream().findFirst();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String approvalId) {
        ApprovalPo approvalPo = approvalMapper.selectPoById(Long.valueOf(approvalId));
        if (approvalPo != null) {
            approvalPo.setRowValid(0);
            approvalMapper.updatePo(approvalPo);
        }
    }

}