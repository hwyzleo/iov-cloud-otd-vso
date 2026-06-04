package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.json.JSONUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateSmallOrderCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderCreateResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderVehicleSnapshotRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelOptionPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;

    @Autowired
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;

    @Autowired
    private SaleModelOptionPolicyRepository saleModelOptionPolicyRepository;

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

    @Test
    void testGetUserOrderWithEnrichment() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String saleModelCode = "SALE_MODEL_DETAIL_" + timestamp;
        String variantCode = "VARIANT_DETAIL_" + timestamp;
        String optionCode1 = "OPT_DETAIL_001_" + timestamp;
        String optionCode2 = "OPT_DETAIL_002_" + timestamp;
        String accountId = "test_user_detail_" + timestamp;

        // 1. 创建订单
        Order order = Order.builder()
                .id("test_order_detail_" + timestamp)
                .orderNo("ORDER_DETAIL_" + timestamp)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(saleModelCode)
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(order);

        // 2. 创建订单快照
        OrderVehicleSnapshotPo snapshot = new OrderVehicleSnapshotPo();
        snapshot.setSnapshotId("SNAPSHOT_DETAIL_" + timestamp);
        snapshot.setOrderId(order.getId());
        snapshot.setSaleModelCode(saleModelCode);
        snapshot.setSaleModelName("测试车型详情_" + timestamp);
        snapshot.setVariantCode(variantCode);
        snapshot.setOptionCodes(JSONUtil.toJsonStr(Arrays.asList(optionCode1, optionCode2)));
        snapshot.setSnapshotVersion(1);
        orderVehicleSnapshotRepository.save(snapshot);

        // 3. 创建 Variant 销售策略
        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .variantCode(variantCode)
                .variantPrice(new BigDecimal("200000"))
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelVariantPolicyRepository.insert(variantPolicy);

        // 4. 创建 Option 销售策略
        SaleModelOptionPolicyPo optionPolicy1 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode1)
                .optionFamilyCode("COLOR")
                .optionPrice(new BigDecimal("8000"))
                .marketingTitle("珍珠白车漆")
                .marketingImage("http://example.com/white.jpg")
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy1);

        SaleModelOptionPolicyPo optionPolicy2 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode2)
                .optionFamilyCode("INTERIOR")
                .optionPrice(new BigDecimal("12000"))
                .marketingTitle("真皮座椅")
                .marketingImage("http://example.com/leather.jpg")
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy2);

        // 5. 调用 getUserOrder()
        OrderDetailResult result = orderAppService.getUserOrder(accountId, order.getOrderNo());

        // 6. 验证结果
        assertNotNull(result, "查询结果不应为空");
        assertEquals(order.getOrderNo(), result.getOrderNo(), "订单号应匹配");

        // 验证展示信息
        assertNotNull(result.getSaleModelImages(), "saleModelImages 不应为空");
        assertEquals(2, result.getSaleModelImages().size(), "应有 2 张图片");
        assertTrue(result.getSaleModelImages().contains("http://example.com/white.jpg"), "应包含白色车漆图片");
        assertTrue(result.getSaleModelImages().contains("http://example.com/leather.jpg"), "应包含真皮座椅图片");

        assertNotNull(result.getSaleModelDesc(), "saleModelDesc 不应为空");
        assertTrue(result.getSaleModelDesc().contains("珍珠白车漆"), "应包含珍珠白车漆");
        assertTrue(result.getSaleModelDesc().contains("真皮座椅"), "应包含真皮座椅");

        assertNotNull(result.getTotalPrice(), "totalPrice 不应为空");
        assertEquals(0, new BigDecimal("220000").compareTo(result.getTotalPrice()), 
                "totalPrice 应为 variantPrice + optionTotalPrice");
    }

    @Test
    void testGetUserOrderWithoutSnapshot() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String accountId = "test_user_no_snapshot_" + timestamp;

        // 创建订单但不创建快照
        Order order = Order.builder()
                .id("test_order_no_snapshot_" + timestamp)
                .orderNo("ORDER_NO_SNAPSHOT_" + timestamp)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(order);

        OrderDetailResult result = orderAppService.getUserOrder(accountId, order.getOrderNo());

        assertNotNull(result, "查询结果不应为空");

        // 没有快照时，这些字段应为空或默认值
        assertTrue(result.getSaleModelImages() == null || result.getSaleModelImages().isEmpty(),
                "没有快照时 saleModelImages 应为空");
        assertTrue(result.getSaleModelDesc() == null || result.getSaleModelDesc().isEmpty(),
                "没有快照时 saleModelDesc 应为空或null");
        assertTrue(result.getTotalPrice() == null || BigDecimal.ZERO.compareTo(result.getTotalPrice()) == 0,
                "没有快照时 totalPrice 应为0或null");
    }
}
