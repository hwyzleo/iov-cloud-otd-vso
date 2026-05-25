package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 品牌编码不存在异常
 *
 * @author hwyz_leo
 */
public class BrandCodeNotExistException extends VsoBaseException {

    public BrandCodeNotExistException(String brandCode) {
        super(VsoErrorCode.SALE_MODEL_CONFIG_TYPE_CODE_NOT_EXIST, "品牌编码[" + brandCode + "]不存在");
    }
}
