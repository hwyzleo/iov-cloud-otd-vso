package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 退款单不存在异常
 *
 * @author VSO Team
 */
public class RefundNotExistException extends VsoBaseException {

    public RefundNotExistException(String message) {
        super(VsoErrorCode.REFUND_NOT_EXIST, message);
    }
}
