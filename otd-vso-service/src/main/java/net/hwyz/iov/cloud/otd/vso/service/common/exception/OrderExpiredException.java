package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单已失效异常（OTD-VSO-DSN-CR-005）
 * <p>
 * 支付发起时订单处于 EXPIRED(960)，客户端据此引导用户重新发起支付。
 *
 * @author VSO Team
 */
public class OrderExpiredException extends VsoBaseException {

    public OrderExpiredException(String message) {
        super(VsoErrorCode.ORDER_EXPIRED, message);
    }
}
