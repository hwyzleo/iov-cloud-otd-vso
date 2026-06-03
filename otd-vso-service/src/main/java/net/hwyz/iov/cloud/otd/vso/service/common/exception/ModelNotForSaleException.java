package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * Model 不可售异常
 *
 * @author VSO Team
 */
public class ModelNotForSaleException extends VsoBaseException {

    public ModelNotForSaleException(String detail) {
        super(VsoErrorCode.MODEL_NOT_FOR_SALE, detail);
    }
}
