package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * VIN冲突异常
 *
 * @author hwyz_leo
 */
public class VinConflictException extends VsoBaseException {

    public VinConflictException(String vin) {
        super(VsoErrorCode.VIN_CONFLICT, "VIN[" + vin + "]冲突");
    }
}
