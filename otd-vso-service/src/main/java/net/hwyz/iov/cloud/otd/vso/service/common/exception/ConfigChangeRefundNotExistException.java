package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 配置变更退款不存在异常
 *
 * @author hwyz_leo
 */
public class ConfigChangeRefundNotExistException extends VsoBaseException {

    public ConfigChangeRefundNotExistException(String refundNo) {
        super(VsoErrorCode.CONFIG_CHANGE_REFUND_NOT_EXIST, "配置变更退款[" + refundNo + "]不存在");
    }
}
