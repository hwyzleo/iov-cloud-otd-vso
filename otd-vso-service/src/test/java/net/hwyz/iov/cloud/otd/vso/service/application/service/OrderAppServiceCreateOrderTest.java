package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateSmallOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateFormalOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderCreateResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单创建测试
 * 验证统一订单号重构后，小订单和正式订单都使用 orderNo
 *
 * @author VSO Team
 */
@SpringBootTest
@Transactional
class OrderAppServiceCreateOrderTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void testCreateSmallOrderShouldHaveOrderNo() {
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
        
        OrderCreateResult result = orderAppService.createSmallOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderId(), "订单ID应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        assertFalse(result.getOrderNo().isEmpty(), "订单号不应该为空字符串");
        
        // 验证数据库中保存的订单有 orderNo
        Optional<Order> savedOrder = orderRepository.findByOrderNo(result.getOrderNo());
        assertTrue(savedOrder.isPresent(), "通过orderNo应该能查询到订单");
        assertEquals(result.getOrderId(), savedOrder.get().getId(), "订单ID应该匹配");
    }

    @Test
    void testCreateFormalOrderShouldHaveOrderNo() {
        String userId = "test_user_" + System.currentTimeMillis();
        
        CreateFormalOrderCmd cmd = CreateFormalOrderCmd.builder()
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
                .regionCode("REGION_001")
                .regionName("测试区域")
                .storeCode("STORE_001")
                .storeName("测试门店")
                .salesCode("SALES_001")
                .salesName("测试销售")
                .build();
        
        OrderCreateResult result = orderAppService.createFormalOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderId(), "订单ID应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        assertFalse(result.getOrderNo().isEmpty(), "订单号不应该为空字符串");
        
        // 验证数据库中保存的订单有 orderNo
        Optional<Order> savedOrder = orderRepository.findByOrderNo(result.getOrderNo());
        assertTrue(savedOrder.isPresent(), "通过orderNo应该能查询到订单");
        assertEquals(result.getOrderId(), savedOrder.get().getId(), "订单ID应该匹配");
    }

    @Test
    void testSmallOrderAndFormalOrderUseSameOrderNoField() {
        // 创建小订单
        String smallOrderUserId = "small_user_" + System.currentTimeMillis();
        CreateSmallOrderCmd smallCmd = CreateSmallOrderCmd.builder()
                .orderSource("capp")
                .userId(smallOrderUserId)
                .name("小订单用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .build();
        
        OrderCreateResult smallResult = orderAppService.createSmallOrder(smallCmd);
        
        // 创建正式订单
        String formalOrderUserId = "formal_user_" + System.currentTimeMillis();
        CreateFormalOrderCmd formalCmd = CreateFormalOrderCmd.builder()
                .orderSource("capp")
                .userId(formalOrderUserId)
                .name("正式订单用户")
                .mobileHash("mobile_hash")
                .idNoHash("id_no_hash")
                .modelCode("MODEL_001")
                .modelName("测试车型")
                .configCode("CONFIG_001")
                .configName("测试配置")
                .colorCode("COLOR_001")
                .colorName("测试颜色")
                .regionCode("REGION_001")
                .regionName("测试区域")
                .storeCode("STORE_001")
                .storeName("测试门店")
                .salesCode("SALES_001")
                .salesName("测试销售")
                .build();
        
        OrderCreateResult formalResult = orderAppService.createFormalOrder(formalCmd);
        
        // 验证两者都有 orderNo，且格式一致
        assertNotNull(smallResult.getOrderNo(), "小订单应该有orderNo");
        assertNotNull(formalResult.getOrderNo(), "正式订单应该有orderNo");
        
        // 验证可以通过 orderNo 查询到订单
        Optional<Order> smallOrder = orderRepository.findByOrderNo(smallResult.getOrderNo());
        Optional<Order> formalOrder = orderRepository.findByOrderNo(formalResult.getOrderNo());
        
        assertTrue(smallOrder.isPresent(), "小订单应该能通过orderNo查询到");
        assertTrue(formalOrder.isPresent(), "正式订单应该能通过orderNo查询到");
        
        // 验证订单类型正确
        assertEquals("small", smallOrder.get().getOrderType(), "小订单类型应该是small");
        assertEquals("formal", formalOrder.get().getOrderType(), "正式订单类型应该是formal");
    }
}
