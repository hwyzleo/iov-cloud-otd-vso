package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 购车协议不存在异常
 *
 * @author VSO Team
 */
public class AgreementNotExistException extends VsoBaseException {

    public AgreementNotExistException(String message) {
        super(VsoErrorCode.AGREEMENT_NOT_EXIST, message);
    }
}
