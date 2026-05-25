package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 补款任务不存在异常
 */
@Slf4j
public class SupplementPaymentNotExistException extends VsoBaseException {

    public SupplementPaymentNotExistException(String supplementaryNo) {
        super(ERROR_CODE_SUPPLEMENT_PAYMENT_NOT_EXIST);
        log.warn("补款任务[{}]不存在", supplementaryNo);
    }
}
