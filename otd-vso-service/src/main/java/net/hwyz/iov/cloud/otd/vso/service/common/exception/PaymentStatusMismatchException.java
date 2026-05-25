package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付状态不匹配异常
 *
 * @author hwyz_leo
 */
public class PaymentStatusMismatchException extends VsoBaseException {

    public PaymentStatusMismatchException(String message) {
        super(VsoErrorCode.PAYMENT_STATUS_MISMATCH, message);
    }
}
