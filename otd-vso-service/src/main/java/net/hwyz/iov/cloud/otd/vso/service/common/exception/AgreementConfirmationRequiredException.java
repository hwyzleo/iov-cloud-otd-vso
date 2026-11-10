package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 购车协议确认缺失异常
 *
 * @author VSO Team
 */
public class AgreementConfirmationRequiredException extends VsoBaseException {

    public AgreementConfirmationRequiredException(String message) {
        super(VsoErrorCode.AGREEMENT_CONFIRMATION_REQUIRED, message);
    }
}
