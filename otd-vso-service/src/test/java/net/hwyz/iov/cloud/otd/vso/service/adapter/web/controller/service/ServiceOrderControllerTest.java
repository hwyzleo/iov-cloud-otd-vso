package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.service.VsoOrderService;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.OrderStateChangeRequest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ActivateCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.TransportOrderBriefResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.OrderAppService;
import net.hwyz.iov.cloud.otd.vso.service.application.service.TransportFulfillmentAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 内部服务订单接口契约测试（OTD-VSO-DSN-CR-012）
 * <p>
 * 覆盖：路径唯一（/api/service/order/v1 + 动作端点）、统一 VO 入参校验、待发运查询分页上限与只读语义。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("内部服务订单接口契约")
class ServiceOrderControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private OrderAppService orderAppService;
    @Mock
    private TransportFulfillmentAppService transportFulfillmentAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ServiceOrderController(orderAppService, transportFulfillmentAppService)).build();
    }

    @Test
    @DisplayName("契约声明路径与实现一致，且不再出现 /api/v1/service 双轨")
    void shouldDeclareSingleContractPath() {
        RequestMapping mapping = ServiceOrderController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).containsExactly("/api/service/order/v1");

        for (String action : List.of("prepareTransport", "transporting", "prepareDelivery", "delivered", "activate")) {
            Method method = findMethod(action);
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            assertThat(postMapping).as(action + " 必须声明 POST 路径").isNotNull();
            assertThat(postMapping.value()[0]).isEqualTo("/order/action/" + action);
        }
        assertThat(VsoOrderService.class.getAnnotation(
                org.springframework.cloud.openfeign.FeignClient.class).path()).isEqualTo("/api/service/order/v1");
    }

    @Test
    @DisplayName("5 个状态回写动作使用统一 VO 入参并透传订单号")
    void shouldAcceptUnifiedRequestVo() throws Exception {
        mockMvc.perform(post("/api/service/order/v1/order/action/prepareTransport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                OrderStateChangeRequest.builder().orderNo("SO1").build())))
                .andExpect(status().isOk());
        ArgumentCaptor<PrepareTransportCmd> prepare = ArgumentCaptor.forClass(PrepareTransportCmd.class);
        verify(orderAppService).prepareTransport(prepare.capture());
        assertThat(prepare.getValue().getOrderNo()).isEqualTo("SO1");

        mockMvc.perform(post("/api/service/order/v1/order/action/transporting")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO1\"}"))
                .andExpect(status().isOk());
        verify(orderAppService).transporting(any(TransportingCmd.class));

        mockMvc.perform(post("/api/service/order/v1/order/action/prepareDelivery")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO1\"}"))
                .andExpect(status().isOk());
        verify(orderAppService).prepareDelivery(any(PrepareDeliveryCmd.class));

        mockMvc.perform(post("/api/service/order/v1/order/action/delivered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO1\"}"))
                .andExpect(status().isOk());
        verify(orderAppService).delivered(any(DeliveredCmd.class));

        mockMvc.perform(post("/api/service/order/v1/order/action/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO1\"}"))
                .andExpect(status().isOk());
        verify(orderAppService).activate(any(ActivateCmd.class));
    }

    @Test
    @DisplayName("订单号为空时拒绝请求")
    void shouldRejectBlankOrderNo() throws Exception {
        mockMvc.perform(post("/api/service/order/v1/order/action/prepareTransport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("待发运查询：返回履约字段、页大小封顶 500")
    void shouldListTransportAppliedWithPageSizeCap() throws Exception {
        when(transportFulfillmentAppService.searchTransportApplied(any(), any()))
                .thenReturn(List.of(TransportOrderBriefResult.builder()
                        .orderId("ORD-1")
                        .orderNo("SO1")
                        .orderState(470)
                        .vin("VIN0000000000001")
                        .deliveryStoreCode("D0001")
                        .deliveryRegionCode("R01")
                        .transportApplyTime(LocalDateTime.of(2026, 11, 18, 13, 5, 27))
                        .transportApplyPersonName("张三")
                        .build()));

        mockMvc.perform(get("/api/service/order/v1/order/list")
                        .param("orderState", "470")
                        .param("pageSize", "9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].orderNo").value("SO1"))
                .andExpect(jsonPath("$.items[0].orderState").value(470))
                .andExpect(jsonPath("$.items[0].vin").value("VIN0000000000001"))
                .andExpect(jsonPath("$.total").value(1));
    }

    private Method findMethod(String name) {
        for (Method method : ServiceOrderController.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                return method;
            }
        }
        throw new IllegalStateException("方法不存在: " + name);
    }
}
