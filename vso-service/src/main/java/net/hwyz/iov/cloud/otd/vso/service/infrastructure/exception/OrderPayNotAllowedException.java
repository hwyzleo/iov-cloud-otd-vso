package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单不允许支付异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class OrderPayNotAllowedException extends VsoBaseException {

    private static final int ERROR_CODE = 401006;

    public OrderPayNotAllowedException(String orderNum) {
        super(ERROR_CODE);
        logger.warn("车辆销售订单[{}]不允许支付", orderNum);
    }

}
