package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.AssignDeliveryPersonCmd;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import net.hwyz.iov.cloud.otd.vso.service.domain.service.OrderDomainService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 分派交付人员测试
 * <p>
 * MPT 传入的是对外订单号（orderNo，形如 VSO2611130001），必须按 order_no 加载订单，
 * 不能按内部订单业务 ID（order_id）加载，否则会误报订单不存在。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("分派交付人员 - 按订单号加载订单")
class OrderAppServiceAssignDeliveryPersonTest {

    private static final String ORDER_NO = "VSO2611130001";

    @Mock
    private OrderDomainService orderDomainService;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderAppService orderAppService;

    @Test
    @DisplayName("按订单号加载订单并保存交付人员")
    void 按订单号加载订单并保存交付人员() {
        Order order = Order.builder()
                .id("ORD1731580800000")
                .orderNo(ORDER_NO)
                .build();
        when(orderDomainService.loadOrderByOrderNo(ORDER_NO)).thenReturn(order);

        orderAppService.assignDeliveryPerson(AssignDeliveryPersonCmd.builder()
                .orderNo(ORDER_NO)
                .deliveryPersonId("1")
                .deliveryPersonName("张三")
                .build());

        verify(orderDomainService).loadOrderByOrderNo(ORDER_NO);
        verify(orderDomainService, never()).loadOrder(anyString());
        verify(orderRepository).save(order);
        assertEquals("1", order.getDeliveryPersonId());
        assertEquals("张三", order.getDeliveryPersonName());
    }
}
