package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 配车/换绑/解绑操作并发冲突异常
 */
@Slf4j
public class BindConflictException extends VsoBaseException {

    public BindConflictException() {
        super(ERROR_CODE_BIND_CONFLICT, "订单正在配车操作中，请稍后再试");
    }

}
