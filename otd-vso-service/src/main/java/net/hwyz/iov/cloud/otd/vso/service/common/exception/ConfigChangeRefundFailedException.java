package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 配置变更退款失败异常
 *
 * @author hwyz_leo
 */
public class ConfigChangeRefundFailedException extends VsoBaseException {

    public ConfigChangeRefundFailedException(String message) {
        super(VsoErrorCode.CONFIG_CHANGE_REFUND_FAILED, message);
    }
}
