package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 重新支付次数已用尽异常（OTD-VSO-DSN-CR-005）
 *
 * @author VSO Team
 */
public class OrderReopenLimitExceededException extends VsoBaseException {

    public OrderReopenLimitExceededException(String message) {
        super(VsoErrorCode.ORDER_REOPEN_LIMIT_EXCEEDED, message);
    }
}
