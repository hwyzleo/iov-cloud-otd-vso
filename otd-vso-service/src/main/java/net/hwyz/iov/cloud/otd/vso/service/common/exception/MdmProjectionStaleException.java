package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * MDM 本地投影过期异常
 *
 * @author hwyz_leo
 */
public class MdmProjectionStaleException extends VsoBaseException {

    public MdmProjectionStaleException(String detail) {
        super(VsoErrorCode.MDM_PROJECTION_STALE, detail);
    }
}
