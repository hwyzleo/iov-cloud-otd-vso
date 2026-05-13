package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateSmallOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderCreateResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单查询测试
 * 验证统一订单号重构后，通过 orderNo 查询订单的功能
 *
 * @author VSO Team
 */
@SpringBootTest
@Transactional
class OrderAppServiceQueryTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testGetByOrderNoShouldReturnOrder() {
        // 先创建一个小订单
        String userId = "test_user_" + System.currentTimeMillis();
        CreateSmallOrderCmd cmd = CreateSmallOrderCmd.builder()
                .orderSource("capp")
                .userId(userId)
                .name("测试用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .build();
        
        OrderCreateResult createResult = orderAppService.createSmallOrder(cmd);
        assertNotNull(createResult.getOrderNo(), "创建订单后应该返回orderNo");
        
        // 通过 orderNo 查询订单
        OrderDetailResult queryResult = orderAppService.getByOrderNo(createResult.getOrderNo());
        
        assertNotNull(queryResult, "查询结果应该不为空");
        assertEquals(createResult.getOrderId(), queryResult.getOrderId(), "订单ID应该匹配");
        assertEquals(createResult.getOrderNo(), queryResult.getOrderNo(), "订单号应该匹配");
    }

    @Test
    void testGetByOrderNoWithNonExistentOrderShouldThrowException() {
        String nonExistentOrderNo = "NON_EXISTENT_ORDER_" + System.currentTimeMillis();
        
        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo(nonExistentOrderNo);
        }, "查询不存在的订单应该抛出异常");
    }

    @Test
    void testGetByOrderNoWithNullShouldThrowException() {
        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo(null);
        }, "传入null应该抛出异常");
    }

    @Test
    void testGetByOrderNoWithEmptyStringShouldThrowException() {
        assertThrows(OrderNotExistException.class, () -> {
            orderAppService.getByOrderNo("");
        }, "传入空字符串应该抛出异常");
    }

    @Test
    void testGetByIdShouldReturnOrder() {
        // 先创建一个小订单
        String userId = "test_user_" + System.currentTimeMillis();
        CreateSmallOrderCmd cmd = CreateSmallOrderCmd.builder()
                .orderSource("capp")
                .userId(userId)
                .name("测试用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .build();
        
        OrderCreateResult createResult = orderAppService.createSmallOrder(cmd);
        assertNotNull(createResult.getOrderId(), "创建订单后应该返回orderId");
        
        // 通过 orderId 查询订单
        OrderDetailResult queryResult = orderAppService.getById(createResult.getOrderId());
        
        assertNotNull(queryResult, "查询结果应该不为空");
        assertEquals(createResult.getOrderId(), queryResult.getOrderId(), "订单ID应该匹配");
        assertEquals(createResult.getOrderNo(), queryResult.getOrderNo(), "订单号应该匹配");
    }
}
