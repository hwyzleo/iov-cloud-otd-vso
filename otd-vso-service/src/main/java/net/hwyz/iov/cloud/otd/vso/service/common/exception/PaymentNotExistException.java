package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付不存在异常
 *
 * @author hwyz_leo
 */
public class PaymentNotExistException extends VsoBaseException {

    public PaymentNotExistException(String paymentNo) {
        super(VsoErrorCode.PAYMENT_NOT_EXIST, "支付[" + paymentNo + "]不存在");
    }
}
