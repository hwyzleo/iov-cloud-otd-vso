package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付金额不一致异常
 *
 * @author VSO Team
 */
public class PaymentAmountMismatchException extends VsoBaseException {

    public PaymentAmountMismatchException(String message) {
        super(VsoErrorCode.PAYMENT_AMOUNT_MISMATCH, message);
    }
}
