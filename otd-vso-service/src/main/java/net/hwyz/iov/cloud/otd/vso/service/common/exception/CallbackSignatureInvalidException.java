package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 闲调签名校验失败异常
 *
 * @author VSO Team
 */
public class CallbackSignatureInvalidException extends VsoBaseException {

    public CallbackSignatureInvalidException(String message) {
        super(VsoErrorCode.CALLBACK_SIGNATURE_INVALID, message);
    }
}
