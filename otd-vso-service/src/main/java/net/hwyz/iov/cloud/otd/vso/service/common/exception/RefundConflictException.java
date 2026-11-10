package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 退款冲突异常
 *
 * @author VSO Team
 */
public class RefundConflictException extends VsoBaseException {

    public RefundConflictException(String message) {
        super(VsoErrorCode.REFUND_CONFLICT, message);
    }
}
