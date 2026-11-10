package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipStaffService;
import net.hwyz.iov.cloud.edd.org.api.vo.DealershipStaffExService;
import net.hwyz.iov.cloud.framework.common.bean.PageResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.DeliveryStaffResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 交付中心人员查询测试（US-008 交付人员分配）
 * <p>
 * 人员来自组织服务远程分页，未交付订单数按交付人员统计；不得返回空实现。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交付中心人员 - 组织服务远程分页/未交付订单数")
class OrderAppServiceDeliveryStaffTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrgDealershipStaffService orgDealershipStaffService;

    @InjectMocks
    private OrderAppService orderAppService;

    @Test
    @DisplayName("交付中心人员列表来自组织服务，并带出未交付订单数")
    void 交付人员列表来自组织服务() {
        DealershipStaffExService staff = DealershipStaffExService.builder()
                .dealershipCode("D001")
                .dealershipName("交付中心A")
                .userId(1001L)
                .userName("staff01")
                .nickName("张三")
                .phonenumber("13800000000")
                .build();
        when(orgDealershipStaffService.searchPage("D001", 1, 20))
                .thenReturn(new PageResult<>(List.of(staff), 1L, 1, 20));
        when(orderRepository.countUndeliveredByDeliveryPersonIds(List.of("1001")))
                .thenReturn(java.util.Map.of("1001", 3));

        PageResult<DeliveryStaffResult> result = orderAppService.searchDeliveryStaff("D001", 1, 20);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(1L, result.getTotal(), "总条数应来自组织服务分页结果");
        DeliveryStaffResult item = result.getItems().get(0);
        assertEquals("staff01", item.getUserName());
        assertEquals("交付中心A", item.getDealershipName());
        assertEquals(3, item.getNotDeliveryOrderCount(), "未交付订单数应按交付人员统计");
        verify(orderRepository).countUndeliveredByDeliveryPersonIds(List.of("1001"));
        verify(orderRepository, org.mockito.Mockito.never()).count(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("多个交付人员的未交付订单数只查一次（批量 group by，不逐个 count）")
    void 多个交付人员只批量统计一次() {
        DealershipStaffExService staff1 = DealershipStaffExService.builder()
                .dealershipCode("D001").userId(1001L).userName("staff01").build();
        DealershipStaffExService staff2 = DealershipStaffExService.builder()
                .dealershipCode("D001").userId(1002L).userName("staff02").build();
        when(orgDealershipStaffService.searchPage("D001", 1, 20))
                .thenReturn(new PageResult<>(List.of(staff1, staff2), 2L, 1, 20));
        when(orderRepository.countUndeliveredByDeliveryPersonIds(List.of("1001", "1002")))
                .thenReturn(java.util.Map.of("1001", 5, "1002", 0));

        PageResult<DeliveryStaffResult> result = orderAppService.searchDeliveryStaff("D001", 1, 20);

        assertEquals(2, result.getItems().size());
        assertEquals(5, result.getItems().get(0).getNotDeliveryOrderCount());
        assertEquals(0, result.getItems().get(1).getNotDeliveryOrderCount());
        verify(orderRepository, org.mockito.Mockito.times(1))
                .countUndeliveredByDeliveryPersonIds(List.of("1001", "1002"));
        verify(orderRepository, org.mockito.Mockito.never())
                .count(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("组织服务无返回时返回空分页，不抛异常")
    void 组织服务无返回时返回空分页() {
        when(orgDealershipStaffService.searchPage("D002", 1, 20)).thenReturn(null);

        PageResult<DeliveryStaffResult> result = orderAppService.searchDeliveryStaff("D002", null, null);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(0L, result.getTotal());
    }

    @Test
    @DisplayName("组织服务分页参数缺省时使用 pageNum=1/pageSize=20")
    void 缺省分页参数() {
        when(orgDealershipStaffService.searchPage("D003", 1, 20))
                .thenReturn(new PageResult<>(List.of(), 0L, 1, 20));

        PageResult<DeliveryStaffResult> result = orderAppService.searchDeliveryStaff("D003", null, null);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        verify(orgDealershipStaffService).searchPage("D003", 1, 20);
    }
}
