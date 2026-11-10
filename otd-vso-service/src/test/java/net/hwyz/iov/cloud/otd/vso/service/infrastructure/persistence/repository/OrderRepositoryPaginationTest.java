package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import com.github.pagehelper.Page;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Order;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderAmountRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.OrderMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.OrderPo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 订单列表分页链路测试（设计 §5.4.2 约束 7）
 * <p>
 * PageHelper 的分页总数只保存在 {@link Page} 实例上；
 * 仓储层若用 stream().collect() 转换会丢失 Page 类型，导致 PageResult.total 退化为当前页条数。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单仓储 - 分页元信息保留")
class OrderRepositoryPaginationTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderAmountRepository orderAmountRepository;

    @InjectMocks
    private OrderRepositoryImpl orderRepository;

    @Test
    @DisplayName("分页查询保留 Page 类型与 total，PageResult.total 不再退化为页大小")
    void 分页查询保留总数() {
        OrderPo po = OrderPo.builder()
                .id(1L)
                .orderId("ORD1")
                .orderNo("SO001")
                .orderState(30)
                .build();
        Page<OrderPo> page = new Page<>(1, 20, true);
        page.setTotal(42L);
        page.add(po);

        when(orderMapper.selectPoByMap(any())).thenReturn(page);

        List<Order> orders = orderRepository.searchByAccount(null, null, null, null, null, null, null);

        assertInstanceOf(Page.class, orders, "分页结果必须保留 Page 类型，否则上层拿不到 total");
        Page<?> paged = (Page<?>) orders;
        assertEquals(42L, paged.getTotal(), "total 必须来自分页插件而不是当前页条数");
        assertEquals(1, orders.size());
        assertEquals("ORD1", orders.get(0).getId());
    }

    @Test
    @DisplayName("非分页查询返回普通列表，不影响既有调用")
    void 非分页查询返回普通列表() {
        OrderPo po = OrderPo.builder()
                .id(2L)
                .orderId("ORD2")
                .orderNo("SO002")
                .orderState(30)
                .build();
        when(orderMapper.selectPoByMap(any())).thenReturn(List.of(po));

        List<Order> orders = orderRepository.searchByAccount(null, null, null, null, null, null, null);

        assertFalse(orders instanceof Page, "未开启分页时不得伪造 Page");
        assertEquals(1, orders.size());
    }
}
