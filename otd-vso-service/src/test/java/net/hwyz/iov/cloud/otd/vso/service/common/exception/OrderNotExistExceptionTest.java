package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderNotExistException 单元测试
 *
 * @author hwyz_leo
 */
class OrderNotExistExceptionTest {

    @Test
    void should_create_with_order_no() {
        String orderNo = "ORDER123";
        OrderNotExistException exception = new OrderNotExistException(orderNo);
        assertEquals(VsoErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
        assertTrue(exception.getMessage().contains(orderNo));
    }

    @Test
    void should_be_instance_of_vso_base_exception() {
        String orderNo = "ORDER123";
        OrderNotExistException exception = new OrderNotExistException(orderNo);
        assertInstanceOf(VsoBaseException.class, exception);
    }

    @Test
    void should_be_instance_of_business_exception() {
        String orderNo = "ORDER123";
        OrderNotExistException exception = new OrderNotExistException(orderNo);
        assertInstanceOf(net.hwyz.iov.cloud.framework.common.exception.BusinessException.class, exception);
    }
}
