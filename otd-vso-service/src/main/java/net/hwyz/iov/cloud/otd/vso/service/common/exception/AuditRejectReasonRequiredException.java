package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditRejectReasonRequiredException extends VsoBaseException {

    public AuditRejectReasonRequiredException() {
        super(ERROR_CODE_AUDIT_REJECT_REASON_REQUIRED, "审核驳回原因必填（分类和详情均不可为空）");
        log.warn("审核驳回操作缺少必填原因");
    }
}
