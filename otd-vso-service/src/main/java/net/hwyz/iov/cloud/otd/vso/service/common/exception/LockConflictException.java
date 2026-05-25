package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 锁定冲突异常
 *
 * @author hwyz_leo
 */
public class LockConflictException extends VsoBaseException {

    public LockConflictException(String message) {
        super(VsoErrorCode.LOCK_CONFLICT, message);
    }
}
