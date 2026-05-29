package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * OptionCode 区域限制异常
 *
 * @author hwyz_leo
 */
public class OptionRegionRestrictedException extends VsoBaseException {

    public OptionRegionRestrictedException(String detail) {
        super(VsoErrorCode.OPTION_REGION_RESTRICTED, detail);
    }
}
