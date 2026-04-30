package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.ApprovalPo;

import java.util.Optional;

/**
 * 审批单仓储接口
 */
public interface ApprovalRepository {

    ApprovalPo save(ApprovalPo approvalPo);

    Optional<ApprovalPo> findByApprovalNo(String approvalNo);

    Optional<ApprovalPo> findByOrderId(String orderId);

    void delete(String approvalId);

}
