package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.OrderQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.OrderListResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderState;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceSearchTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private OrderRepository orderRepository;

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
                .brandCode("BRAND001")
                .saleModel("SALE_MODEL_001")
                .ownerRegionCode("REGION001")
                .buildConfigCode("BUILD_CONFIG_001")
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
        assertEquals(order.getOrderType().name().toLowerCase(), result.getOrderType(), "订单类型编码应该匹配");
        assertEquals("小订单", result.getOrderTypeName(), "订单类型名称应该是小订单");
        assertEquals(order.getOrderSource(), result.getOrderSource(), "订单来源编码应该匹配");
        assertEquals("C端自主下单", result.getOrderSourceName(), "订单来源名称应该是C端自主下单");
        assertEquals(order.getBrandCode(), result.getBrandCode(), "品牌编码应该匹配");
        assertEquals(order.getSaleModel(), result.getSaleModel(), "销售车型编码应该匹配");
        assertEquals(order.getOwnerRegionCode(), result.getOwnerRegionCode(), "归属区域编码应该匹配");
        assertEquals(order.getBuildConfigCode(), result.getBuildConfigCode(), "生产配置编码应该匹配");
    }

    @Test
    void testSearchWithOrderTypeFormal() {
        Order order = Order.builder()
                .id("test_order_formal_" + System.currentTimeMillis())
                .orderNo("FORMAL_ORDER_NO_" + System.currentTimeMillis())
                .orderType(OrderType.FORMAL)
                .orderSource("sales")
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
        assertEquals("formal", result.getOrderType(), "订单类型编码应该是 formal");
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
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(orderCapp);
        
        Order orderStore = Order.builder()
                .id("test_store_" + timestamp)
                .orderNo("ORDER_STORE_" + timestamp)
                .orderSource("store")
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(orderStore);
        
        Order orderImport = Order.builder()
                .id("test_import_" + timestamp)
                .orderNo("ORDER_IMPORT_" + timestamp)
                .orderSource("import")
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
                .orderType(null)
                .orderSource(null)
                .orderState(OrderState.EARNEST_MONEY_UNPAID)
                .build();
        orderRepository.save(order);
        
        OrderQuery query = OrderQuery.builder()
                .orderNo(order.getOrderNo())
                .build();
        
        List<OrderListResult> results = orderAppService.search(query);
        
        assertFalse(results.isEmpty(), "应该找到订单");
        
        OrderListResult result = results.get(0);
        assertNull(result.getOrderType(), "订单类型编码应该为空");
        assertEquals("", result.getOrderTypeName(), "订单类型名称应该为空字符串");
        assertNull(result.getOrderSource(), "订单来源编码应该为空");
        assertEquals("", result.getOrderSourceName(), "订单来源名称应该为空字符串");
    }
}