package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * VIN冲突异常
 */
@Slf4j
public class VinConflictException extends VsoBaseException {

    private final String vin;

    public VinConflictException(String vin) {
        super(ERROR_CODE_VIN_CONFLICT);
        this.vin = vin;
        log.warn("VIN [{}] 已被其他订单占用", vin);
    }

    public String getVin() {
        return vin;
    }

}