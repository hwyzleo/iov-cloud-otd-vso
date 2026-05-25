package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 补缴支付不存在异常
 *
 * @author hwyz_leo
 */
public class SupplementPaymentNotExistException extends VsoBaseException {

    public SupplementPaymentNotExistException(String paymentNo) {
        super(VsoErrorCode.SUPPLEMENT_PAYMENT_NOT_EXIST, "补缴支付[" + paymentNo + "]不存在");
    }
}
