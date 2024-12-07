package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售车型配置已锁定异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class SaleModelConfigHasLockedException extends VsoBaseException {

    private static final int ERROR_CODE = 401008;

    public SaleModelConfigHasLockedException(String orderNum) {
        super(ERROR_CODE);
        logger.warn("订单[{}]销售车型配置已锁定", orderNum);
    }

}
