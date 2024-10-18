package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单非法删除异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class OrderIllegalDeleteException extends VsoBaseException {

    private static final int ERROR_CODE = 401005;

    public OrderIllegalDeleteException(String orderNum) {
        super(ERROR_CODE);
        logger.warn("车辆销售订单[{}]非法删除", orderNum);
    }

}
