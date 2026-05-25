package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VsoBaseException 单元测试
 *
 * @author hwyz_leo
 */
class VsoBaseExceptionTest {

    @Test
    void should_create_with_error_code() {
        VsoErrorCode errorCode = VsoErrorCode.ORDER_NOT_EXIST;
        VsoBaseException exception = new VsoBaseException(errorCode);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(errorCode.getMessage(), exception.getMessage());
    }

    @Test
    void should_create_with_error_code_and_detail() {
        VsoErrorCode errorCode = VsoErrorCode.ORDER_NOT_EXIST;
        String detail = "自定义错误消息";
        VsoBaseException exception = new VsoBaseException(errorCode, detail);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(detail, exception.getMessage());
    }

    @Test
    void should_create_with_error_code_and_cause() {
        VsoErrorCode errorCode = VsoErrorCode.INTERNAL_ERROR;
        Throwable cause = new RuntimeException("原始异常");
        VsoBaseException exception = new VsoBaseException(errorCode, cause);
        assertEquals(errorCode, exception.getErrorCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void should_be_instance_of_business_exception() {
        VsoErrorCode errorCode = VsoErrorCode.ORDER_NOT_EXIST;
        VsoBaseException exception = new VsoBaseException(errorCode);
        assertInstanceOf(net.hwyz.iov.cloud.framework.common.exception.BusinessException.class, exception);
    }
}
