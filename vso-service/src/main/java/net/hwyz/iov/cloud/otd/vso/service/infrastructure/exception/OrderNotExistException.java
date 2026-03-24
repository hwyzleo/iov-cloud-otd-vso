package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class OrderNotExistException extends VsoBaseException {

    public OrderNotExistException(String orderNum) {
        super(ERROR_CODE_ORDER_NOT_EXIST);
        logger.warn("车辆销售订单[{}]不存在", orderNum);
    }

}
