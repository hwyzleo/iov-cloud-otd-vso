package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 构建配置不匹配异常
 *
 * @author hwyz_leo
 */
public class BuildConfigNotMatchedException extends VsoBaseException {

    public BuildConfigNotMatchedException(String message) {
        super(VsoErrorCode.CONFIGURATION_NOT_MATCHED, message);
    }
}
