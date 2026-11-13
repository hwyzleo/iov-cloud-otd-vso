package net.hwyz.iov.cloud.otd.vso.service.application.service;

import feign.FeignException;
import feign.Request;
import feign.Response;
import net.hwyz.iov.cloud.edd.org.api.enums.DealershipServiceType;
import net.hwyz.iov.cloud.edd.org.api.service.OrgDealershipService;
import net.hwyz.iov.cloud.edd.org.api.vo.Dealership;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderDealershipVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.DealershipQuery;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DealershipQueryFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.VsoBaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 订单详情门店选项应用服务测试（OTD-VSO-DSN-CR-008 §8）
 * <p>
 * 覆盖：类型映射、区域与经纬度透传、VO 映射（含距离 null）、上游顺序保持、
 * 空列表正常返回、上游异常与降级 null 均映射 201060、非法类型不调用上游。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("订单详情门店选项 - 应用服务")
class OrderDealershipAppServiceTest {

    @Mock
    private OrgDealershipService orgDealershipService;

    @InjectMocks
    private OrderDealershipAppService orderDealershipAppService;

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

    @Test
    @DisplayName("S 类型：映射为 ORG 枚举并完整透传区域与经纬度")
    void 销售类型透传全部条件() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        orderDealershipAppService.listDealerships(query("S"));

        verify(orgDealershipService).getDealershipList(eq(DealershipServiceType.S), eq("110000"),
                eq("110100"), eq("110101"), eq("116.404"), eq("39.915"));
    }

    @Test
    @DisplayName("D 类型：映射为交付中心枚举")
    void 交付类型映射() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        orderDealershipAppService.listDealerships(query("D"));

        verify(orgDealershipService).getDealershipList(eq(DealershipServiceType.D), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("VO 映射：字段一一对应，距离为 null 时保持 null 且顺序与上游一致")
    void 映射字段与顺序() {
        Dealership first = Dealership.builder().code("D001").name("北京交付中心")
                .address("北京市朝阳区").distance(1.25).build();
        Dealership second = Dealership.builder().code("D002").name("上海交付中心")
                .address("上海市浦东新区").distance(null).build();
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(first, second));

        List<OrderDealershipVo> voList = orderDealershipAppService.listDealerships(query("D"));

        assertEquals(2, voList.size());
        assertEquals("D001", voList.get(0).getCode());
        assertEquals("北京交付中心", voList.get(0).getName());
        assertEquals("北京市朝阳区", voList.get(0).getAddress());
        assertEquals(1.25, voList.get(0).getDistance());
        assertEquals("D002", voList.get(1).getCode());
        assertNull(voList.get(1).getDistance(), "无法计算距离时必须保持 null，不得伪造 0");
    }

    @Test
    @DisplayName("上游返回空列表：返回空列表而非异常")
    void 空列表正常返回() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        List<OrderDealershipVo> voList = orderDealershipAppService.listDealerships(query("S"));

        assertTrue(voList.isEmpty(), "无符合条件的门店时应返回空列表");
    }

    @Test
    @DisplayName("降级返回 null：映射 201060，不得当作空列表")
    void 降级返回null转业务错误() {
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        DealershipQueryFailedException exception = assertThrows(DealershipQueryFailedException.class,
                () -> orderDealershipAppService.listDealerships(query("S")));
        assertEquals("201060", exception.getErrorCode().getCode());
    }

    @Test
    @DisplayName("Feign 调用异常：映射 201060 并保留原因")
    void Feign异常转业务错误() {
        FeignException feignException = feignException(500);
        when(orgDealershipService.getDealershipList(any(), any(), any(), any(), any(), any()))
                .thenThrow(feignException);

        DealershipQueryFailedException exception = assertThrows(DealershipQueryFailedException.class,
                () -> orderDealershipAppService.listDealerships(query("S")));
        assertEquals("201060", exception.getErrorCode().getCode());
        assertSame(feignException, exception.getCause());
    }

    @Test
    @DisplayName("非法或缺失 serviceType：201001 且不调用 ORG（含售后 A）")
    void 非法类型不调用上游() {
        for (String illegal : new String[]{null, "", "A", "X", "s"}) {
            VsoBaseException exception = assertThrows(VsoBaseException.class,
                    () -> orderDealershipAppService.listDealerships(query(illegal)));
            assertEquals("201001", exception.getErrorCode().getCode());
        }
        verifyNoInteractions(orgDealershipService);
    }

    @Test
    @DisplayName("只读证明：应用服务仅依赖 ORG 门店服务，不持有订单仓储/状态机/时间线/支付依赖")
    void 仅依赖门店服务() {
        java.lang.reflect.Field[] fields = OrderDealershipAppService.class.getDeclaredFields();
        long instanceFields = java.util.Arrays.stream(fields)
                .filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers()))
                .count();
        assertEquals(1, instanceFields, "应用服务只允许注入 OrgDealershipService 一个实例依赖");
        assertEquals(OrgDealershipService.class, fields[fields.length - 1].getType());
    }

    private FeignException feignException(int status) {
        Request request = Request.create(Request.HttpMethod.GET,
                "http://edd-org/service/dealership/list", Map.of(), null, StandardCharsets.UTF_8, null);
        Response response = Response.builder()
                .status(status)
                .reason("Internal Server Error")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("OrgDealershipService#getDealershipList", response);
    }
}
