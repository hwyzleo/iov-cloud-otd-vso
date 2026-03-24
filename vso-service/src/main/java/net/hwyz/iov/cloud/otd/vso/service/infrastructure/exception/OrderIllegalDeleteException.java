package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单非法删除异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class OrderIllegalDeleteException extends VsoBaseException {

    public OrderIllegalDeleteException(String orderNum) {
        super(ERROR_CODE_ORDER_ILLEGAL_DELETE);
        logger.warn("车辆销售订单[{}]非法删除", orderNum);
    }

}
