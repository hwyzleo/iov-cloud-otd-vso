package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 订单不存在异常
 *
 * @author hwyz_leo
 */
public class OrderNotExistException extends VsoBaseException {

    public OrderNotExistException(String orderNo) {
        super(VsoErrorCode.ORDER_NOT_EXIST, "车辆销售订单[" + orderNo + "]不存在");
    }
}
