package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单当前状态不允许重新支付异常（OTD-VSO-DSN-CR-005）
 *
 * @author VSO Team
 */
public class OrderReopenNotAllowedException extends VsoBaseException {

    public OrderReopenNotAllowedException(String message) {
        super(VsoErrorCode.ORDER_REOPEN_NOT_ALLOWED, message);
    }
}
