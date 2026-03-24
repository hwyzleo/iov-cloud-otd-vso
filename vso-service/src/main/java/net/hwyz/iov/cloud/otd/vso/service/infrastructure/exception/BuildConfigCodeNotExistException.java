package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 生产配置代码不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class BuildConfigCodeNotExistException extends VsoBaseException {

    public BuildConfigCodeNotExistException(String modelCode, String exteriorCode, String interiorCode, String wheelCode,
                                            String tireCode, String spareTireCode, String adasCode, String seatCode) {
        super(ERROR_CODE_BUILD_CONFIG_CODE_NOT_EXIST);
        logger.warn("车型[{}]外饰[{}]内饰[{}]轮毂[{}]轮胎[{}]备胎[{}]智驾[{}]座椅[{}]的生产配置代码不存在", modelCode, exteriorCode,
                interiorCode, wheelCode, tireCode, spareTireCode, adasCode, seatCode);
    }

}
