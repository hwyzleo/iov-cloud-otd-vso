package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.service.VsoOrderService;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.OrderStateChangeRequest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.handler.VsoWebRequestExceptionHandler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeliveredCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareDeliveryCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PrepareTransportCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.TransportingCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.TransportOrderBriefResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.BindingFactsQueryAppService;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    @Mock
    private BindingFactsQueryAppService bindingFactsQueryAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ServiceOrderController(orderAppService, transportFulfillmentAppService,
                                bindingFactsQueryAppService))
                .setControllerAdvice(new VsoWebRequestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("契约声明路径与实现一致，且不再出现 /api/v1/service 双轨")
    void shouldDeclareSingleContractPath() {
        RequestMapping mapping = ServiceOrderController.class.getAnnotation(RequestMapping.class);
        assertThat(mapping.value()).containsExactly("/api/service/order/v1");

        for (String action : List.of("prepareTransport", "transporting", "prepareDelivery", "delivered")) {
            Method method = findMethod(action);
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            assertThat(postMapping).as(action + " 必须声明 POST 路径").isNotNull();
            assertThat(postMapping.value()[0]).isEqualTo("/order/action/" + action);
        }
        assertThat(Arrays.stream(ServiceOrderController.class.getDeclaredMethods())
                .noneMatch(method -> "activate".equals(method.getName())))
                .as("OTD-VSO-DSN-CR-016：activate 契约必须删除")
                .isTrue();
        assertThatThrownBy(() -> VsoOrderService.class.getMethod("activate", OrderStateChangeRequest.class))
                .as("OTD-VSO-DSN-CR-016：Feign 声明不得保留 activate")
                .isInstanceOf(NoSuchMethodException.class);
        assertThat(VsoOrderService.class.getAnnotation(
                org.springframework.cloud.openfeign.FeignClient.class).path()).isEqualTo("/api/service/order/v1");
    }

    @Test
    @DisplayName("4 个状态回写动作使用统一 VO 入参并透传订单号")
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
        ArgumentCaptor<DeliveredCmd> delivered = ArgumentCaptor.forClass(DeliveredCmd.class);
        verify(orderAppService).delivered(delivered.capture());
        assertThat(delivered.getValue().getOrderNo()).isEqualTo("SO1");
        assertThat(delivered.getValue().getOrderId()).isNull();
        assertThat(delivered.getValue().getOperatorId()).isEqualTo("system");
        assertThat(delivered.getValue().getOperatorRole()).isEqualTo("service");
        assertThat(delivered.getValue().getOperateSource()).isEqualTo("delivery_service");
        assertThat(delivered.getValue().getDeliveryRemark()).isEqualTo("外部交付服务确认已交付");
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

    @Test
    @DisplayName("待发运查询缺必填参数时返回 400 参数校验失败，而非 500 系统内部错误")
    void shouldReturnBadRequestWhenRequiredParameterMissing() throws Exception {
        mockMvc.perform(get("/api/service/order/v1/order/list"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("201001"))
                .andExpect(jsonPath("$.message").value("缺少必填参数: orderState"));
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
