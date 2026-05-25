package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 补缴支付已过期异常
 *
 * @author hwyz_leo
 */
public class SupplementPaymentExpiredException extends VsoBaseException {

    public SupplementPaymentExpiredException(String paymentNo) {
        super(VsoErrorCode.SUPPLEMENT_PAYMENT_EXPIRED, "补缴支付[" + paymentNo + "]已过期");
    }
}
