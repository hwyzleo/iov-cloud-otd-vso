package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * Configuration 不匹配异常
 *
 * @author hwyz_leo
 */
public class ConfigurationNotMatchedException extends VsoBaseException {

    public ConfigurationNotMatchedException(String message) {
        super(VsoErrorCode.CONFIGURATION_NOT_MATCHED, message);
    }
}
