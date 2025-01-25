package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 车型配置代码不存在异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class ModelConfigCodeNotExistException extends VsoBaseException {

    private static final int ERROR_CODE = 401002;

    public ModelConfigCodeNotExistException(String modelCode, String exteriorCode, String interiorCode, String wheelCode,
                                            String spareTireCode, String adasCode, String seatCode) {
        super(ERROR_CODE);
        logger.warn("车型[{}]外饰[{}]内饰[{}]车轮[{}]备胎[{}]智驾[{}]座椅[{}]的车型配置代码不存在", modelCode, exteriorCode,
                interiorCode, wheelCode, spareTireCode, adasCode, seatCode);
    }

}
