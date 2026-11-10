package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 购车协议版本冲突异常
 * <p>
 * 同一车型（含 GLOBAL）+ 协议代码 + 版本号唯一；冲突时返回业务错误码，不向上暴露数据库异常。
 *
 * @author VSO Team
 */
public class AgreementVersionConflictException extends VsoBaseException {

    public AgreementVersionConflictException(String message) {
        super(VsoErrorCode.AGREEMENT_VERSION_CONFLICT, message);
    }
}
