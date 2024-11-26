package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;


import net.hwyz.iov.cloud.framework.common.exception.BaseException;

/**
 * 车辆销售订单服务基础异常
 *
 * @author hwyz_leo
 */
public class VsoBaseException extends BaseException {

    private static final int ERROR_CODE = 401000;

    public VsoBaseException(String message) {
        super(ERROR_CODE, message);
    }

    public VsoBaseException(int errorCode) {
        super(errorCode);
    }

    public VsoBaseException(int errorCode, String message) {
        super(errorCode, message);
    }

}
