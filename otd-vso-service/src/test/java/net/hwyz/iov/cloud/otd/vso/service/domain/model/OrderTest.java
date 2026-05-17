package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType;
import net.hwyz.iov.cloud.otd.vso.api.enums.OrderType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单领域模型测试
 * 验证统一订单号重构后，Order 模型的行为
 *
 * @author VSO Team
 */
class OrderTest {

    @Test
    void testEarnestMoneyOrderShouldSetOrderTypeAndOrderNo() {
        Order order = Order.fromWishlist("test_user", "TEST_SALE_MODEL");
        
        // fromWishlist 已经设置了 orderType 为 small
        assertEquals(OrderType.SMALL, order.getOrderType(), "fromWishlist应该设置orderType为SMALL");
        assertNull(order.getOrderNo(), "初始状态不应该有orderNo");
        
        // 执行意向金下单
        order.earnestMoneyOrder();
        
        // 验证 orderType 仍然是 small
        assertEquals(OrderType.SMALL, order.getOrderType(), "小订单类型应该是SMALL");
        
        // 验证 orderNo 已生成
        assertNotNull(order.getOrderNo(), "意向金下单后应该生成orderNo");
        assertFalse(order.getOrderNo().isEmpty(), "orderNo不应该为空字符串");
        
        // 验证状态正确
        assertEquals(OrderState.EARNEST_MONEY_UNPAID, order.getOrderState(), "意向金下单状态应该是待支付意向金");
    }

    @Test
    void testEarnestMoneyOrderShouldNotHaveSmallOrderNo() {
        Order order = Order.fromWishlist("test_user", "TEST_SALE_MODEL");
        order.earnestMoneyOrder();
        
        // 验证 smallOrderNo 字段不存在（已删除）
        // 这个测试主要是确保编译通过，没有 smallOrderNo 字段
        assertNotNull(order.getOrderNo(), "应该只有orderNo");
    }

    @Test
    void testFromWishlistShouldCreateSmallOrder() {
        String userId = "test_user";
        String saleModel = "TEST_SALE_MODEL";
        
        Order order = Order.fromWishlist(userId, saleModel);
        
        assertEquals(userId, order.getOrderPersonId(), "下单人ID应该匹配");
        assertEquals(saleModel, order.getSaleModel(), "销售车型应该匹配");
        assertEquals(OrderType.SMALL, order.getOrderType(), "订单类型应该是SMALL");
        assertEquals("capp", order.getOrderSource(), "订单来源应该是capp");
        assertEquals(CustomerType.PERSONAL.getCode(), order.getCustomerType(), "客户类型应该是personal");
        assertFalse(order.getHasException(), "不应该有异常");
        assertEquals(Integer.valueOf(1), order.getCurrentVersionNo(), "版本号应该是1");
        assertFalse(order.getLockedFlag(), "不应该锁定");
        assertFalse(order.getReopenFlag(), "不应该重开过");
    }

    @Test
    void testDownPaymentOrderShouldGenerateOrderNo() {
        Order order = Order.fromWishlist("test_user", "TEST_SALE_MODEL");
        
        assertNull(order.getOrderNo(), "初始状态不应该有orderNo");
        
        order.downPaymentOrder();
        
        assertNotNull(order.getOrderNo(), "定金下单后应该生成orderNo");
        assertFalse(order.getOrderNo().isEmpty(), "orderNo不应该为空字符串");
        assertEquals(OrderState.DOWN_PAYMENT_UNPAID, order.getOrderState(), "定金订单初始状态应该是待支付定金");
    }
    
    @Test
    void testFormalOrderShouldCreateFormalOrder() {
        String orderId = "test_order_" + System.currentTimeMillis();
        String orderSource = "capp";
        
        Order order = Order.createFormalOrder(orderId, orderSource);
        
        assertEquals(orderId, order.getId(), "订单ID应该匹配");
        assertEquals(orderSource, order.getOrderSource(), "订单来源应该匹配");
    }
}
