package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 审批拒绝原因必填异常
 *
 * @author hwyz_leo
 */
public class AuditRejectReasonRequiredException extends VsoBaseException {

    public AuditRejectReasonRequiredException() {
        super(VsoErrorCode.AUDIT_REJECT_REASON_REQUIRED);
    }
}
