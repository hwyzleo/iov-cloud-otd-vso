package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售车型不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class SaleModelNotExistException extends VsoBaseException {

    private static final int ERROR_CODE = 401003;

    public SaleModelNotExistException(String saleCode) {
        super(ERROR_CODE);
        logger.warn("销售车型[{}]不存在", saleCode);
    }

}
