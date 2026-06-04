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
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.SaleModelVariantPolicyRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderVehicleSnapshotPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelOptionPolicyPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.SaleModelVariantPolicyPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单搜索测试")
class OrderAppServiceSearchTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderVehicleSnapshotRepository orderVehicleSnapshotRepository;
    @Mock
    private SaleModelVariantPolicyRepository saleModelVariantPolicyRepository;
    @Mock
    private SaleModelOptionPolicyRepository saleModelOptionPolicyRepository;
    @Mock
    private SaleModelRepository saleModelRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    private Order buildOrder(String id, String orderNo, OrderType orderType, String orderSource, OrderState orderState) {
        return Order.builder()
                .id(id)
                .orderNo(orderNo)
                .orderType(orderType)
                .orderSource(orderSource)
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .ownerRegionCode("REGION001")
                .configurationCode("BUILD_CONFIG_001")
                .currentVersionNo(1)
                .orderState(orderState)
                .build();
    }

    @Test
    @DisplayName("空查询应返回空列表")
    void testSearchWithNullQuery() {
        List<OrderListResult> results = orderAppService.search(null);

        assertNotNull(results, "返回结果应该不为空");
        assertTrue(results.isEmpty(), "空查询应该返回空列表");
    }

    @Test
    @DisplayName("按订单号搜索应返回匹配订单")
    void testSearchWithOrderNo() {
        String orderId = "test_order_" + System.currentTimeMillis();
        String orderNo = "TEST_ORDER_NO_" + System.currentTimeMillis();

        Order order = buildOrder(orderId, orderNo, OrderType.SMALL, "capp", OrderState.EARNEST_MONEY_UNPAID);

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(order));

        OrderQuery query = OrderQuery.builder()
                .orderNo(orderNo)
                .build();

        List<OrderListResult> results = orderAppService.search(query);

        assertNotNull(results, "返回结果应该不为空");
        assertFalse(results.isEmpty(), "应该找到订单");

        OrderListResult result = results.get(0);
        assertEquals(orderNo, result.getOrderNo(), "订单号应该匹配");
        assertEquals("small", result.getOrderType().toLowerCase(), "订单类型编码应该匹配");
        assertEquals("小订单", result.getOrderTypeName(), "订单类型名称应该是小订单");
        assertEquals("capp", result.getOrderSource(), "订单来源编码应该匹配");
        assertEquals("C端自主下单", result.getOrderSourceName(), "订单来源名称应该是C端自主下单");
        assertEquals("BRAND001", result.getBrandCode(), "品牌编码应该匹配");
        assertEquals("SALE_MODEL_001", result.getSaleModel(), "销售车型编码应该匹配");
        assertEquals("REGION001", result.getOwnerRegionCode(), "归属区域编码应该匹配");
        assertEquals("BUILD_CONFIG_001", result.getConfigurationCode(), "生产配置编码应该匹配");
    }

    @Test
    @DisplayName("搜索正式订单应返回正确的类型信息")
    void testSearchWithOrderTypeFormal() {
        String orderId = "test_order_formal_" + System.currentTimeMillis();
        String orderNo = "FORMAL_ORDER_NO_" + System.currentTimeMillis();

        Order order = buildOrder(orderId, orderNo, OrderType.FORMAL, "sales", OrderState.DOWN_PAYMENT_UNPAID);

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(order));

        OrderQuery query = OrderQuery.builder()
                .orderNo(orderNo)
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
    @DisplayName("搜索应正确设置订单来源名称")
    void testSearchWithMultipleOrderSources() {
        String timestamp = String.valueOf(System.currentTimeMillis());

        Order orderCapp = buildOrder("test_capp_" + timestamp, "ORDER_CAPP_" + timestamp,
                OrderType.SMALL, "capp", OrderState.EARNEST_MONEY_UNPAID);
        Order orderStore = buildOrder("test_store_" + timestamp, "ORDER_STORE_" + timestamp,
                OrderType.SMALL, "store", OrderState.EARNEST_MONEY_UNPAID);
        Order orderImport = buildOrder("test_import_" + timestamp, "ORDER_IMPORT_" + timestamp,
                OrderType.SMALL, "import", OrderState.EARNEST_MONEY_UNPAID);

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Arrays.asList(orderCapp, orderStore, orderImport));

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
    @DisplayName("搜索应正确设置小订单类型名称")
    void testSearchWithEmptyOrderType() {
        String orderId = "test_empty_type_" + System.currentTimeMillis();
        String orderNo = "EMPTY_TYPE_ORDER_" + System.currentTimeMillis();

        Order order = buildOrder(orderId, orderNo, OrderType.SMALL, "capp", OrderState.EARNEST_MONEY_UNPAID);

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(order));

        OrderQuery query = OrderQuery.builder()
                .orderNo(orderNo)
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
    @DisplayName("搜索结果应包含快照和销售策略的展示信息")
    void testSearchWithEnrichment() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String saleModelCode = "SALE_MODEL_ENRICH_" + timestamp;
        String variantCode = "VARIANT_001_" + timestamp;
        String optionCode1 = "OPT_001_" + timestamp;
        String optionCode2 = "OPT_002_" + timestamp;
        String orderId = "test_order_enrich_" + timestamp;
        String orderNo = "ORDER_ENRICH_" + timestamp;

        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel(saleModelCode)
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();

        OrderVehicleSnapshotPo snapshot = new OrderVehicleSnapshotPo();
        snapshot.setSnapshotId("SNAPSHOT_" + timestamp);
        snapshot.setOrderId(orderId);
        snapshot.setSaleModelCode(saleModelCode);
        snapshot.setSaleModelName("测试车型_" + timestamp);
        snapshot.setVariantCode(variantCode);
        snapshot.setOptionCodes(JSONUtil.toJsonStr(Arrays.asList(optionCode1, optionCode2)));
        snapshot.setSnapshotVersion(1);

        SaleModelVariantPolicyPo variantPolicy = SaleModelVariantPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .variantCode(variantCode)
                .variantPrice(new BigDecimal("100000"))
                .saleStatus("active")
                .build();

        SaleModelOptionPolicyPo optionPolicy1 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode1)
                .optionFamilyCode("COLOR")
                .optionPrice(new BigDecimal("5000"))
                .marketingTitle("高级内饰")
                .marketingImage("http://example.com/img1.jpg")
                .saleStatus("active")
                .build();

        SaleModelOptionPolicyPo optionPolicy2 = SaleModelOptionPolicyPo.builder()
                .saleModelCode(saleModelCode)
                .optionCode(optionCode2)
                .optionFamilyCode("WHEEL")
                .optionPrice(new BigDecimal("3000"))
                .marketingTitle("运动轮毂")
                .marketingImage("http://example.com/img2.jpg")
                .saleStatus("active")
                .build();

        SaleModelPo saleModelPo = SaleModelPo.builder()
                .id(1L)
                .saleModelCode(saleModelCode)
                .modelName("测试车型_" + timestamp)
                .build();

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(order));
        when(orderVehicleSnapshotRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(snapshot));
        when(saleModelVariantPolicyRepository.findBySaleModelCodeAndVariantCode(saleModelCode, variantCode))
                .thenReturn(Optional.of(variantPolicy));
        when(saleModelOptionPolicyRepository.findBySaleModelCodeAndOptionCodes(eq(saleModelCode), anyList()))
                .thenReturn(Arrays.asList(optionPolicy1, optionPolicy2));
        when(saleModelRepository.findBySaleModelCode(saleModelCode))
                .thenReturn(Optional.of(saleModelPo));

        OrderQuery query = OrderQuery.builder()
                .orderNo(orderNo)
                .build();
        List<OrderListResult> results = orderAppService.search(query);

        assertFalse(results.isEmpty(), "应该找到订单");
        OrderListResult result = results.get(0);

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
    @DisplayName("搜索无快照订单应返回默认展示信息")
    void testSearchWithoutSnapshot() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String orderId = "test_order_no_snapshot_" + timestamp;
        String orderNo = "ORDER_NO_SNAPSHOT_" + timestamp;

        Order order = Order.builder()
                .id(orderId)
                .orderNo(orderNo)
                .orderType(OrderType.SMALL)
                .orderSource("capp")
                .customerType("personal")
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .configurationCode("CONFIG_001")
                .currentVersionNo(1)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();

        when(orderRepository.search(any(), any(), any(), any(), any(), any()))
                .thenReturn(Collections.singletonList(order));
        when(orderVehicleSnapshotRepository.findByOrderId(orderId))
                .thenReturn(Optional.empty());

        OrderQuery query = OrderQuery.builder()
                .orderNo(orderNo)
                .build();
        List<OrderListResult> results = orderAppService.search(query);

        assertFalse(results.isEmpty(), "应该找到订单");
        OrderListResult result = results.get(0);

        assertTrue(result.getSaleModelImages() == null || result.getSaleModelImages().isEmpty(),
                "没有快照时 saleModelImages 应为空");
        assertEquals("", result.getSaleModelDesc(), "没有快照时 saleModelDesc 应为空字符串");
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalPrice()),
                "没有快照时 totalPrice 应为0");
    }
}
