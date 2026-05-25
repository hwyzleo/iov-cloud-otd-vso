package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付冲突异常
 *
 * @author hwyz_leo
 */
public class PaymentConflictException extends VsoBaseException {

    public PaymentConflictException(String message) {
        super(VsoErrorCode.PAYMENT_CONFLICT, message);
    }
}
