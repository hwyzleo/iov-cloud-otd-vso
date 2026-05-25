package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * VIN无效异常
 *
 * @author hwyz_leo
 */
public class VinInvalidException extends VsoBaseException {

    public VinInvalidException(String vin) {
        super(VsoErrorCode.VIN_INVALID, "VIN[" + vin + "]无效");
    }
}
