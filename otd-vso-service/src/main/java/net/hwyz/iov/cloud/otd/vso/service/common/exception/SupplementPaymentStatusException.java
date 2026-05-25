package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 补款任务状态不正确异常
 */
@Slf4j
public class SupplementPaymentStatusException extends VsoBaseException {

    public SupplementPaymentStatusException(String supplementaryNo, String status) {
        super(ERROR_CODE_SUPPLEMENT_PAYMENT_STATUS_NOT_ALLOWED);
        log.warn("补款任务[{}]状态[{}]不正确", supplementaryNo, status);
    }
}
