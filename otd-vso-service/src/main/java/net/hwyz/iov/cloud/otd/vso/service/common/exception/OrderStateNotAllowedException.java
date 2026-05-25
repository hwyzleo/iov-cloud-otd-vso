package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单状态不允许异常
 *
 * @author hwyz_leo
 */
public class OrderStateNotAllowedException extends VsoBaseException {

    public OrderStateNotAllowedException(String message) {
        super(VsoErrorCode.ORDER_STATE_NOT_ALLOWED, message);
    }
}
