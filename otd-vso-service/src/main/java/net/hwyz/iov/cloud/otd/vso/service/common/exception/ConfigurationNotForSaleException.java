package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * Configuration 未列入销售白名单异常
 *
 * @author hwyz_leo
 */
public class ConfigurationNotForSaleException extends VsoBaseException {

    public ConfigurationNotForSaleException(String detail) {
        super(VsoErrorCode.CONFIGURATION_NOT_FOR_SALE, detail);
    }
}
