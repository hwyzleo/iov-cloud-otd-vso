package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 补缴支付状态不允许异常
 *
 * @author hwyz_leo
 */
public class SupplementPaymentStatusException extends VsoBaseException {

    public SupplementPaymentStatusException(String message) {
        super(VsoErrorCode.SUPPLEMENT_PAYMENT_STATUS_NOT_ALLOWED, message);
    }
}
