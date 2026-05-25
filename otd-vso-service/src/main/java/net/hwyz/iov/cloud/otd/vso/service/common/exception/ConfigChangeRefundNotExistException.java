package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 改配退款任务不存在异常
 */
@Slf4j
public class ConfigChangeRefundNotExistException extends VsoBaseException {

    public ConfigChangeRefundNotExistException(String refundNo) {
        super(ERROR_CODE_CONFIG_CHANGE_REFUND_NOT_EXIST);
        log.warn("改配退款任务[{}]不存在", refundNo);
    }
}
