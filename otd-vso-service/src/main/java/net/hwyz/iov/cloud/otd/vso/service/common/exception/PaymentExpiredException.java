package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付单已过期异常
 *
 * @author VSO Team
 */
public class PaymentExpiredException extends VsoBaseException {

    public PaymentExpiredException(String message) {
        super(VsoErrorCode.PAYMENT_EXPIRED, message);
    }
}
