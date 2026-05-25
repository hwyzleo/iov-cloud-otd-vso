package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 审批重提交次数超限异常
 *
 * @author hwyz_leo
 */
public class AuditResubmitLimitExceededException extends VsoBaseException {

    public AuditResubmitLimitExceededException(int maxResubmitCount) {
        super(VsoErrorCode.AUDIT_RESUBMIT_LIMIT_EXCEEDED, "审批重提交次数已超限，最大次数: " + maxResubmitCount);
    }
}
