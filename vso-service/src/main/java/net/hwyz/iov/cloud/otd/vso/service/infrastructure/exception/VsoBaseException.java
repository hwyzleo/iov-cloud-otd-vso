package net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception;


import net.hwyz.iov.cloud.framework.common.exception.BaseException;

/**
 * 车辆销售订单服务基础异常
 *
 * @author hwyz_leo
 */
public class VsoBaseException extends BaseException {

    private static final int ERROR_CODE = 301000;
    protected static final int ERROR_CODE_SALE_MODEL_CONFIG_TYPE_CODE_NOT_EXIST = 301001;
    protected static final int ERROR_CODE_BUILD_CONFIG_CODE_NOT_EXIST = 301002;
    protected static final int ERROR_CODE_SALE_MODEL_NOT_EXIST = 301003;
    protected static final int ERROR_CODE_ORDER_NOT_EXIST = 301004;
    protected static final int ERROR_CODE_ORDER_ILLEGAL_DELETE = 301005;
    protected static final int ERROR_CODE_ORDER_STATE_NOT_ALLOWED = 301006;
    protected static final int ERROR_CODE_ACCOUNT_NOT_EXIST = 301007;
    protected static final int ERROR_CODE_SALE_MODEL_CONFIG_HAS_LOCKED = 301008;

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
