package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 改配退款失败异常
 */
@Slf4j
public class ConfigChangeRefundFailedException extends VsoBaseException {

    public ConfigChangeRefundFailedException(String refundNo, String reason) {
        super(ERROR_CODE_CONFIG_CHANGE_REFUND_FAILED);
        log.warn("改配退款任务[{}]失败: {}", refundNo, reason);
    }
}
