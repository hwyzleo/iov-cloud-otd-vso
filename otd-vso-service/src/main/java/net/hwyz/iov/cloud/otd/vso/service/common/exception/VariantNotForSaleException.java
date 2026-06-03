package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * Variant 不可售异常
 *
 * @author VSO Team
 */
public class VariantNotForSaleException extends VsoBaseException {

    public VariantNotForSaleException(String detail) {
        super(VsoErrorCode.VARIANT_NOT_FOR_SALE, detail);
    }
}
