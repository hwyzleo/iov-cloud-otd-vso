package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.json.JSONUtil;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
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
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceSearchTest {

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
    void testSearchWithNullQuery() {
        List<OrderListResult> results = orderAppService.search(null);
        
        assertNotNull(results, "返回结果应该不为空");
        assertTrue(results.isEmpty(), "空查询应该返回空列表");
    }

    @Test
    void testSearchWithOrderNo() {
        Order order = Order.builder()
                .id("test_order_" + System.currentTimeMillis())
                .orderNo("TEST_ORDER_NO_" + System.currentTimeMillis())
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .ownerRegionCode("REGION001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(order);
        
        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        
        List<OrderListResult> results = orderAppService.search(query);
        
        assertNotNull(results, "返回结果应该不为空");
        assertFalse(results.isEmpty(), "应该找到订单");
        
        OrderListResult result = results.get(0);
        assertEquals(order.getOrderNo(), result.getOrderNo(), "订单号应该匹配");
        assertEquals(order.getOrderType().name().toLowerCase(), result.getOrderType().toLowerCase(), "订单类型编码应该匹配");
        assertEquals("小订单", result.getOrderTypeName(), "订单类型名称应该是小订单");
        assertEquals(order.getOrderSource(), result.getOrderSource(), "订单来源编码应该匹配");
        assertEquals("C端自主下单", result.getOrderSourceName(), "订单来源名称应该是C端自主下单");
        assertEquals(order.getBrandCode(), result.getBrandCode(), "品牌编码应该匹配");
        assertEquals(order.getSaleModel(), result.getSaleModel(), "销售车型编码应该匹配");
        assertEquals(order.getOwnerRegionCode(), result.getOwnerRegionCode(), "归属区域编码应该匹配");
        assertEquals(order.getConfigurationCode(), result.getConfigurationCode(), "生产配置编码应该匹配");
    }

    @Test
    void testSearchWithOrderTypeFormal() {
        Order order = Order.builder()
                .id("test_order_formal_" + System.currentTimeMillis())
                .orderNo("FORMAL_ORDER_NO_" + System.currentTimeMillis())
                .orderType(OrderType.FORMAL)
                .orderSource("sales")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.DOWN_PAYMENT_UNPAID)
                .build();
        orderRepository.save(order);
        
        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        
        List<OrderListResult> results = orderAppService.search(query);
        
        assertNotNull(results, "返回结果应该不为空");
        assertFalse(results.isEmpty(), "应该找到订单");
        
        OrderListResult result = results.get(0);
        assertEquals("formal", result.getOrderType().toLowerCase(), "订单类型编码应该是 formal");
        assertEquals("正式订单", result.getOrderTypeName(), "订单类型名称应该是正式订单");
        assertEquals("sales", result.getOrderSource(), "订单来源编码应该是 sales");
        assertEquals("销售代客下单", result.getOrderSourceName(), "订单来源名称应该是销售代客下单");
    }

    @Test
    void testSearchWithMultipleOrderSources() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        Order orderCapp = Order.builder()
                .id("test_capp_" + timestamp)
                .orderNo("ORDER_CAPP_" + timestamp)
                .orderSource("capp")
                .orderType(OrderType.SMALL)
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(orderCapp);
        
        Order orderStore = Order.builder()
                .id("test_store_" + timestamp)
                .orderNo("ORDER_STORE_" + timestamp)
                .orderSource("store")
                .orderType(OrderType.SMALL)
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(orderStore);
        
        Order orderImport = Order.builder()
                .id("test_import_" + timestamp)
                .orderNo("ORDER_IMPORT_" + timestamp)
                .orderSource("import")
                .orderType(OrderType.SMALL)
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(orderImport);
        
        List<OrderListResult> results = orderAppService.search(OrderQuery.builder().build());
        
        assertTrue(results.size() >= 3, "应该至少找到3个订单");
        
        OrderListResult cappResult = results.stream()
                .filter(r -> "capp".equals(r.getOrderSource()))
                .findFirst()
                .orElse(null);
        assertNotNull(cappResult, "应该找到capp来源的订单");
        assertEquals("C端自主下单", cappResult.getOrderSourceName());
        
        OrderListResult storeResult = results.stream()
                .filter(r -> "store".equals(r.getOrderSource()))
                .findFirst()
                .orElse(null);
        assertNotNull(storeResult, "应该找到store来源的订单");
        assertEquals("门店代客下单", storeResult.getOrderSourceName());
        
        OrderListResult importResult = results.stream()
                .filter(r -> "import".equals(r.getOrderSource()))
                .findFirst()
                .orElse(null);
        assertNotNull(importResult, "应该找到import来源的订单");
        assertEquals("外部导入", importResult.getOrderSourceName());
    }

    @Test
    void testSearchWithEmptyOrderType() {
        Order order = Order.builder()
                .id("test_empty_type_" + System.currentTimeMillis())
                .orderNo("EMPTY_TYPE_ORDER_" + System.currentTimeMillis())
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(order);
        
        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        
        List<OrderListResult> results = orderAppService.search(query);
        
        assertFalse(results.isEmpty(), "应该找到订单");
        
        OrderListResult result = results.get(0);
        assertEquals("small", result.getOrderType().toLowerCase(), "订单类型编码应该是 small");
        assertEquals("小订单", result.getOrderTypeName(), "订单类型名称应该是小订单");
        assertEquals("capp", result.getOrderSource(), "订单来源编码应该是 capp");
        assertEquals("C端自主下单", result.getOrderSourceName(), "订单来源名称应该是C端自主下单");
    }

    @Test
    void testSearchWithEnrichment() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String saleModelCode = "SALE_MODEL_ENRICH_" + timestamp;
        String variantCode = "VARIANT_001_" + timestamp;
        String optionCode1 = "OPT_001_" + timestamp;
        String optionCode2 = "OPT_002_" + timestamp;

        // 1. 创建订单
        Order order = Order.builder()
                .id("test_order_enrich_" + timestamp)
                .orderNo("ORDER_ENRICH_" + timestamp)
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
        snapshot.setSnapshotId("SNAPSHOT_" + timestamp);
        snapshot.setOrderId(order.getId());
        snapshot.setSaleModelCode(saleModelCode);
        snapshot.setSaleModelName("测试车型_" + timestamp);
        snapshot.setVariantCode(variantCode);
        snapshot.setOptionCodes(JSONUtil.toJsonStr(Arrays.asList(optionCode1, optionCode2)));
        snapshot.setSnapshotVersion(1);
        orderVehicleSnapshotRepository.save(snapshot);

        // 3. 创建 Variant 销售策略
        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .variantCode(variantCode)
                .variantPrice(new BigDecimal("100000"))
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelVariantPolicyRepository.insert(variantPolicy);

        // 4. 创建 Option 销售策略
        SaleModelOptionPolicyPo optionPolicy1 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode1)
                .optionFamilyCode("COLOR")
                .optionPrice(new BigDecimal("5000"))
                .marketingTitle("高级内饰")
                .marketingImage("http://example.com/img1.jpg")
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy1);

        SaleModelOptionPolicyPo optionPolicy2 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode2)
                .optionFamilyCode("WHEEL")
                .optionPrice(new BigDecimal("3000"))
                .marketingTitle("运动轮毂")
                .marketingImage("http://example.com/img2.jpg")
                .saleStatus("active")
                .createTime(new Timestamp(System.currentTimeMillis()))
                .build();
        saleModelOptionPolicyRepository.save(optionPolicy2);

        // 5. 调用 search()
        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        List<OrderListResult> results = orderAppService.search(query);

        // 6. 验证结果
        assertFalse(results.isEmpty(), "应该找到订单");
        OrderListResult result = results.get(0);

        // 验证展示信息
        assertNotNull(result.getSaleModelImages(), "saleModelImages 不应为空");
        assertEquals(2, result.getSaleModelImages().size(), "应有 2 张图片");
        assertTrue(result.getSaleModelImages().contains("http://example.com/img1.jpg"), "应包含图片1");
        assertTrue(result.getSaleModelImages().contains("http://example.com/img2.jpg"), "应包含图片2");

        assertNotNull(result.getSaleModelDesc(), "saleModelDesc 不应为空");
        assertTrue(result.getSaleModelDesc().contains("高级内饰"), "应包含高级内饰");
        assertTrue(result.getSaleModelDesc().contains("运动轮毂"), "应包含运动轮毂");

        assertNotNull(result.getTotalPrice(), "totalPrice 不应为空");
        assertEquals(0, new BigDecimal("108000").compareTo(result.getTotalPrice()), "totalPrice 应为 variantPrice + optionTotalPrice");
    }

    @Test
    void testSearchWithoutSnapshot() {
        String timestamp = String.valueOf(System.currentTimeMillis());

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

        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        List<OrderListResult> results = orderAppService.search(query);

        assertFalse(results.isEmpty(), "应该找到订单");
        OrderListResult result = results.get(0);

        // 没有快照时，这些字段应为空或默认值
        assertTrue(result.getSaleModelImages() == null || result.getSaleModelImages().isEmpty(),
                "没有快照时 saleModelImages 应为空");
        assertEquals("", result.getSaleModelDesc(), "没有快照时 saleModelDesc 应为空字符串");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalPrice()),
                "没有快照时 totalPrice 应为0");
    }
}
