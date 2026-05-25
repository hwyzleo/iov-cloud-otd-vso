package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 绑定冲突异常
 *
 * @author hwyz_leo
 */
public class BindConflictException extends VsoBaseException {

    public BindConflictException(String message) {
        super(VsoErrorCode.BIND_CONFLICT, message);
    }
}
