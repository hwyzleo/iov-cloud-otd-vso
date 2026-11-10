package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单归属校验失败异常
 *
 * @author VSO Team
 */
public class OrderAccessDeniedException extends VsoBaseException {

    public OrderAccessDeniedException(String message) {
        super(VsoErrorCode.ORDER_ACCESS_DENIED, message);
    }
}
