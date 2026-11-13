package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.vo.Dealership;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderDealershipVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.DealershipQuery;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DealershipQueryFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 订单详情门店选项集成测试（OTD-VSO-DSN-CR-008 §8）
 * <p>
 * 说明：本地真实 Spring 上下文内验证「Controller/应用服务装配 → ORG Feign 契约 → VO 映射」链路，
 * EDD-ORG 以 Mock 隔离；只读证明通过对比查询前后 vso_order / vso_order_timeline / vso_payment
 * 记录数完成。不代表真实跨服务 E2E 或生产验收。
 *
 * @author VSO Team
 */
@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "iov.kafka.topic-provisioning.enabled=false"
})
@Transactional
@DisplayName("订单详情门店选项 - 集成测试")
class OrderDealershipIntegrationTest {

    @Autowired
    private OrderDealershipAppService orderDealershipAppService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private OrgDealershipService orgDealershipService;

    private DealershipQuery query(String serviceType) {
        return DealershipQuery.builder()
                .serviceType(serviceType)
                .provinceCode("110000")
                .cityCode("110100")
                .countyCode("110101")
                .requestLon("116.404")
                .requestLat("39.915")
                .build();
    }

    private long count(String table) {
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
        return total == null ? 0L : total;
    }

    @Test
    @DisplayName("查询交付中心：返回 VSO 自有 VO 且不写入订单、时间线与支付")
    void 查询交付中心只读() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(
                        Dealership.builder().code("D001").name("北京交付中心")
                                .address("北京市顺义区").distance(12.34).build(),
                        Dealership.builder().code("D002").name("天津交付中心")
                                .address("天津市西青区").distance(null).build()));

        long orderBefore = count("vso_order");
        long timelineBefore = count("vso_order_timeline");
        long paymentBefore = count("vso_payment");

        List<OrderDealershipVo> voList = orderDealershipAppService.listDealerships(query("D"));

        assertEquals(2, voList.size());
        assertEquals("D001", voList.get(0).getCode());
        assertEquals("北京交付中心", voList.get(0).getName());
        assertEquals("北京市顺义区", voList.get(0).getAddress());
        assertEquals(12.34, voList.get(0).getDistance());
        assertEquals("D002", voList.get(1).getCode());
        assertNull(voList.get(1).getDistance());

        assertEquals(orderBefore, count("vso_order"), "查询门店选项不得写入订单");
        assertEquals(timelineBefore, count("vso_order_timeline"), "查询门店选项不得追加订单时间线");
        assertEquals(paymentBefore, count("vso_payment"), "查询门店选项不得写入支付");
    }

    @Test
    @DisplayName("ORG 降级返回 null：201060，且不产生任何订单副作用")
    void 上游降级不写入订单() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        long orderBefore = count("vso_order");
        long timelineBefore = count("vso_order_timeline");

        DealershipQueryFailedException exception = assertThrows(DealershipQueryFailedException.class,
                () -> orderDealershipAppService.listDealerships(query("S")));

        assertEquals("201060", exception.getErrorCode().getCode());
        assertEquals(orderBefore, count("vso_order"));
        assertEquals(timelineBefore, count("vso_order_timeline"));
    }
}
