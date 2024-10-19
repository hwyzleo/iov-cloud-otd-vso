package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums.OrderState;

/**
 * 订单当前状态不支持此操作异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class OrderStateNotAllowedException extends VsoBaseException {

    private static final int ERROR_CODE = 401006;

    public OrderStateNotAllowedException(String orderNum, OrderState orderState, String operation) {
        super(ERROR_CODE);
        logger.warn("车辆销售订单[{}]当前状态[{}]不支持此操作[{}]", orderNum, orderState, operation);
    }

}
