package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付操作并发冲突异常
 */
@Slf4j
public class PaymentConflictException extends VsoBaseException {

    public PaymentConflictException() {
        super(ERROR_CODE_PAYMENT_CONFLICT, "订单正在支付中，请稍后再试");
    }

}
