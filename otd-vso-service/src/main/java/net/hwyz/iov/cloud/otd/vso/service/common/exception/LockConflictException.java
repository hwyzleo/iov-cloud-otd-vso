package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 锁单/退款/改配/关单操作并发冲突异常
 */
@Slf4j
public class LockConflictException extends VsoBaseException {

    public LockConflictException() {
        super(ERROR_CODE_LOCK_CONFLICT, "订单正在处理中，请稍后再试");
    }

}
