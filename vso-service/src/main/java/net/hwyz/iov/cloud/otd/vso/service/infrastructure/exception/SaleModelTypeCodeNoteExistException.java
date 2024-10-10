package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售车型类型代码不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class SaleModelTypeCodeNoteExistException extends VsoBaseException {

    private static final int ERROR_CODE = 401001;

    public SaleModelTypeCodeNoteExistException(String saleCode, String saleModelType, String saleModelTypeCode) {
        super(ERROR_CODE);
        logger.warn("销售车型[{}]类型[{}]代码[{}]不存在", saleCode, saleModelType, saleModelTypeCode);
    }

}
