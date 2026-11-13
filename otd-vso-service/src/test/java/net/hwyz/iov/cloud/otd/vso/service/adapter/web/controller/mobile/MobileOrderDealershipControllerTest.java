package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.framework.web.handler.GlobalExceptionHandler;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.OrderDealershipVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.DealershipQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderDealershipAppService;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.DealershipQueryFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C 端订单详情门店选项契约测试（OTD-VSO-DSN-CR-008 §4/§8）
 * <p>
 * 覆盖：S/D 成功、非法/缺失/A 类型拒绝且不触达应用服务、区域与经纬度完整透传、
 * 空列表成功返回、上游失败错误码不被 Controller 吞掉。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("C 端订单详情门店选项 - 契约")
class MobileOrderDealershipControllerTest {

    private static final String BASE = "/api/mobile/vso/v1/dealership/list";

    @Mock
    private OrderDealershipAppService orderDealershipAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MobileOrderDealershipController controller = new MobileOrderDealershipController(orderDealershipAppService);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    private OrderDealershipVo vo(String code, String name, String address, Double distance) {
        OrderDealershipVo vo = new OrderDealershipVo();
        vo.setCode(code);
        vo.setName(name);
        vo.setAddress(address);
        vo.setDistance(distance);
        return vo;
    }

    @Test
    @DisplayName("serviceType=S：成功返回 code/name/address/distance")
    void 销售门店查询成功() throws Exception {
        when(orderDealershipAppService.listDealerships(any()))
                .thenReturn(List.of(vo("S001", "北京销售门店", "北京市朝阳区", 2.5)));

        mockMvc.perform(get(BASE).param("serviceType", "S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data[0].code").value("S001"))
                .andExpect(jsonPath("$.data[0].name").value("北京销售门店"))
                .andExpect(jsonPath("$.data[0].address").value("北京市朝阳区"))
                .andExpect(jsonPath("$.data[0].distance").value(2.5));
    }

    @Test
    @DisplayName("serviceType=D：成功返回交付中心选项")
    void 交付中心查询成功() throws Exception {
        when(orderDealershipAppService.listDealerships(any()))
                .thenReturn(List.of(vo("D001", "北京交付中心", "北京市顺义区", null)));

        mockMvc.perform(get(BASE).param("serviceType", "D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("D001"))
                .andExpect(jsonPath("$.data[0].name").value("北京交付中心"));
    }

    @Test
    @DisplayName("区域与经纬度按契约完整透传，不附加 orderNo/account 等参数")
    void 查询条件完整透传() throws Exception {
        when(orderDealershipAppService.listDealerships(any())).thenReturn(List.of());

        mockMvc.perform(get(BASE)
                        .param("serviceType", "S")
                        .param("provinceCode", "110000")
                        .param("cityCode", "110100")
                        .param("countyCode", "110101")
                        .param("requestLon", "116.404")
                        .param("requestLat", "39.915"))
                .andExpect(status().isOk());

        ArgumentCaptor<DealershipQuery> captor = ArgumentCaptor.forClass(DealershipQuery.class);
        verify(orderDealershipAppService).listDealerships(captor.capture());
        DealershipQuery query = captor.getValue();
        assertEquals("S", query.getServiceType());
        assertEquals("110000", query.getProvinceCode());
        assertEquals("110100", query.getCityCode());
        assertEquals("110101", query.getCountyCode());
        assertEquals("116.404", query.getRequestLon());
        assertEquals("39.915", query.getRequestLat());
    }

    @Test
    @DisplayName("无可选门店：成功返回空列表")
    void 空列表成功返回() throws Exception {
        when(orderDealershipAppService.listDealerships(any())).thenReturn(List.of());

        mockMvc.perform(get(BASE).param("serviceType", "D"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("serviceType 缺失/非法/售后 A：返回 201001 且不触达应用服务与 ORG")
    void 非法服务类型拒绝() throws Exception {
        for (String illegal : new String[]{null, "", "A", "X", "s", "S,D"}) {
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder = get(BASE);
            if (illegal != null) {
                builder = builder.param("serviceType", illegal);
            }
            mockMvc.perform(builder)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("201001"));
        }
        verifyNoInteractions(orderDealershipAppService);
    }

    @Test
    @DisplayName("ORG 查询失败：201060 向上抛出，Controller 不返回伪成功空列表")
    void 上游失败不吞异常() throws Exception {
        when(orderDealershipAppService.listDealerships(any()))
                .thenThrow(new DealershipQueryFailedException("订单详情门店选项查询失败"));

        mockMvc.perform(get(BASE).param("serviceType", "S"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("201060"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("接口不接收 orderNo/accountId 及品牌车型等订单专属或过滤参数")
    void 不接受订单专属参数() {
        Set<String> declared = Arrays.stream(MobileOrderDealershipController.class.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .flatMap(method -> Arrays.stream(method.getParameters()))
                .map(parameter -> parameter.getAnnotation(RequestParam.class))
                .filter(Objects::nonNull)
                .map(RequestParam::value)
                .collect(Collectors.toSet());

        assertEquals(Set.of("serviceType", "provinceCode", "cityCode", "countyCode",
                "requestLon", "requestLat"), declared);
        assertFalse(declared.contains("orderNo"));
        assertFalse(declared.contains("accountId"));
    }
}
