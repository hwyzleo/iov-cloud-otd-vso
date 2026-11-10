package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 退款状态不允许异常
 *
 * @author VSO Team
 */
public class RefundStatusNotAllowedException extends VsoBaseException {

    public RefundStatusNotAllowedException(String message) {
        super(VsoErrorCode.REFUND_STATUS_NOT_ALLOWED, message);
    }
}
