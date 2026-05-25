package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 账号不存在异常
 *
 * @author hwyz_leo
 */
public class AccountNotExistException extends VsoBaseException {

    public AccountNotExistException(String accountNo) {
        super(VsoErrorCode.ACCOUNT_NOT_EXIST, "账号[" + accountNo + "]不存在");
    }
}
