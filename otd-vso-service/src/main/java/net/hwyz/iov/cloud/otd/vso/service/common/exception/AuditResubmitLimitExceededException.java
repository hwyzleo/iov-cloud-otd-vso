package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditResubmitLimitExceededException extends VsoBaseException {

    public static final int RESUBMIT_LIMIT = 3;

    public AuditResubmitLimitExceededException(String orderId) {
        super(ERROR_CODE_AUDIT_RESUBMIT_LIMIT_EXCEEDED, "审核重提次数超限（最多3次），订单将自动关闭");
        log.warn("订单[{}]审核重提次数超限{}，禁止继续重提", orderId, RESUBMIT_LIMIT);
    }
}
