package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 构建配置编码不存在异常
 *
 * @author hwyz_leo
 */
public class BuildConfigCodeNotExistException extends VsoBaseException {

    public BuildConfigCodeNotExistException(String buildConfigCode) {
        super(VsoErrorCode.CONFIGURATION_CODE_NOT_EXIST, "配置编码[" + buildConfigCode + "]不存在");
    }
}
