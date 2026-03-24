package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 账号不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class AccountNotExistException extends VsoBaseException {

    public AccountNotExistException(String accountId) {
        super(ERROR_CODE_ACCOUNT_NOT_EXIST);
        logger.warn("账号[{}]不存在", accountId);
    }

}
