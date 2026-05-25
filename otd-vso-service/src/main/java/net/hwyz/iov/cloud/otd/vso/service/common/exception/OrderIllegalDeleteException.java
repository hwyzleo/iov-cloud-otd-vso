package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单非法删除异常
 *
 * @author hwyz_leo
 */
public class OrderIllegalDeleteException extends VsoBaseException {

    public OrderIllegalDeleteException(String orderNo) {
        super(VsoErrorCode.ORDER_ILLEGAL_DELETE, "订单[" + orderNo + "]不允许删除");
    }
}
