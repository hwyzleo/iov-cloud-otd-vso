package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 销售型号不存在异常
 *
 * @author hwyz_leo
 */
public class SaleModelNotExistException extends VsoBaseException {

    public SaleModelNotExistException(String saleModelCode) {
        super(VsoErrorCode.SALE_MODEL_NOT_EXIST, "销售型号[" + saleModelCode + "]不存在");
    }
}
