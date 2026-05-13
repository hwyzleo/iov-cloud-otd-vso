package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PaymentStatusMismatchException extends VsoBaseException {
    public PaymentStatusMismatchException(String paymentNo, String currentStatus) {
        super(ERROR_CODE_PAYMENT_STATUS_MISMATCH, "支付单状态不匹配，当前状态：" + currentStatus);
        log.warn("支付单[{}]状态不匹配，当前状态：{}", paymentNo, currentStatus);
    }
}