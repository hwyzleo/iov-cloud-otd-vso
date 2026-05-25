package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 销售型号配置类型编码不存在异常
 *
 * @author hwyz_leo
 */
public class SaleModelConfigTypeCodeNotExistException extends VsoBaseException {

    public SaleModelConfigTypeCodeNotExistException(String typeCode) {
        super(VsoErrorCode.SALE_MODEL_CONFIG_TYPE_CODE_NOT_EXIST, "销售型号配置类型编码[" + typeCode + "]不存在");
    }
}
