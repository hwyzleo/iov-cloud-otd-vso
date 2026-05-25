package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * VIN无效异常
 */
@Slf4j
public class VinInvalidException extends VsoBaseException {

    private final String vin;

    public VinInvalidException(String vin) {
        super(ERROR_CODE_VIN_INVALID);
        this.vin = vin;
        log.warn("VIN [{}] 无效或状态不可用", vin);
    }

    public String getVin() {
        return vin;
    }

}