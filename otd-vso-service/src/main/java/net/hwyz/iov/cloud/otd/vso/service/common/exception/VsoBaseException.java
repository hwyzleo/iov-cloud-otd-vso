package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import net.hwyz.iov.cloud.framework.common.exception.BusinessException;
import net.hwyz.iov.cloud.framework.common.exception.ErrorCode;

/**
 * 车辆销售订单服务基础异常
 *
 * @author hwyz_leo
 */
public class VsoBaseException extends BusinessException {

    public VsoBaseException(ErrorCode errorCode) {
        super(errorCode);
    }

    public VsoBaseException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public VsoBaseException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
