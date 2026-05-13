package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentNotExistException extends VsoBaseException {
    public PaymentNotExistException(String paymentNo) {
        super(ERROR_CODE_PAYMENT_NOT_EXIST, "支付单不存在：" + paymentNo);
        log.warn("支付单不存在：{}", paymentNo);
    }
}