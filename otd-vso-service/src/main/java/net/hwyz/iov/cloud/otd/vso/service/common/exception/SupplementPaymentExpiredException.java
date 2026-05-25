package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 补款任务已过期异常
 */
@Slf4j
public class SupplementPaymentExpiredException extends VsoBaseException {

    public SupplementPaymentExpiredException(String supplementaryNo) {
        super(ERROR_CODE_SUPPLEMENT_PAYMENT_EXPIRED);
        log.warn("补款任务[{}]已过期", supplementaryNo);
    }
}
