package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 构建配置编码不存在异常
 *
 * @author hwyz_leo
 */
public class BuildConfigCodeNotExistException extends VsoBaseException {

    public BuildConfigCodeNotExistException(String buildConfigCode) {
        super(VsoErrorCode.BUILD_CONFIG_CODE_NOT_EXIST, "构建配置编码[" + buildConfigCode + "]不存在");
    }
}
