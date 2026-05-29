package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * OptionCode 不可售异常
 *
 * @author hwyz_leo
 */
public class OptionNotForSaleException extends VsoBaseException {

    public OptionNotForSaleException(String detail) {
        super(VsoErrorCode.OPTION_NOT_FOR_SALE, detail);
    }
}
