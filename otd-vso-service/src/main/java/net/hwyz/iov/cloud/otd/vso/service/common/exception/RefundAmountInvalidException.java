package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 退款金额不合法异常
 *
 * @author VSO Team
 */
public class RefundAmountInvalidException extends VsoBaseException {

    public RefundAmountInvalidException(String message) {
        super(VsoErrorCode.REFUND_AMOUNT_INVALID, message);
    }
}
