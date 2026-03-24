package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售车型不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class SaleModelNotExistException extends VsoBaseException {

    public SaleModelNotExistException(String saleCode) {
        super(ERROR_CODE_SALE_MODEL_NOT_EXIST);
        logger.warn("销售车型[{}]不存在", saleCode);
    }

}
