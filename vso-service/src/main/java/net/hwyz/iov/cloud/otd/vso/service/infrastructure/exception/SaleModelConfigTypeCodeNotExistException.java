package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 销售车型配置类型代码不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class SaleModelConfigTypeCodeNotExistException extends VsoBaseException {

    public SaleModelConfigTypeCodeNotExistException(String saleCode, String saleModelConfigType, String saleModelConfigTypeCode) {
        super(ERROR_CODE_SALE_MODEL_CONFIG_TYPE_CODE_NOT_EXIST);
        logger.warn("销售车型[{}]配置类型[{}]代码[{}]不存在", saleCode, saleModelConfigType, saleModelConfigTypeCode);
    }

}
