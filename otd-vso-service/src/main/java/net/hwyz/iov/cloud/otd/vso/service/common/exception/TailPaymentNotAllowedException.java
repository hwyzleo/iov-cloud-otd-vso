package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 尾款支付条件不满足异常（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
public class TailPaymentNotAllowedException extends VsoBaseException {

    public TailPaymentNotAllowedException(String message) {
        super(VsoErrorCode.TAIL_PAYMENT_NOT_ALLOWED, message);
    }
}
