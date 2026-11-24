package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingDeliveryStatus;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingEvidenceType;
import net.hwyz.iov.cloud.otd.vso.api.enums.BindingLookupStatus;
import net.hwyz.iov.cloud.otd.vso.api.service.VsoOrderService;
import net.hwyz.iov.cloud.framework.web.jackson.IovIsoWriteModule;
import net.hwyz.iov.cloud.otd.vso.api.vo.request.BindingFactsQueryRequest;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.handler.VsoWebRequestExceptionHandler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.query.BindingFactsQuery;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.BindingFactsResult;
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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 绑车事实查询接口契约测试（OTD-VSO-DSN-CR-017，覆盖 AT-01、AT-04、AT-08、AT-09）
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("绑车事实查询 - 接口契约")
class ServiceOrderControllerBindingFactsTest {

    private static final String PATH = "/api/service/order/v1/order/binding-facts/query";

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
        // 使用框架 CR-006 时间写出模块（ISO-8601 + 固定 3 位毫秒），与生产序列化口径一致
        ObjectMapper mvcObjectMapper = new ObjectMapper();
        mvcObjectMapper.registerModule(new IovIsoWriteModule(ZoneId.of("UTC")));
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ServiceOrderController(orderAppService, transportFulfillmentAppService,
                                bindingFactsQueryAppService))
                .setControllerAdvice(new VsoWebRequestExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mvcObjectMapper))
                .build();
    }

    @Test
    @DisplayName("契约声明：唯一内部前缀 + 候选 POST 路径，且不新增第二套路由")
    void shouldDeclareSingleContractPath() throws Exception {
        PostMapping mapping = ServiceOrderController.class
                .getMethod("queryBindingFacts", BindingFactsQueryRequest.class)
                .getAnnotation(PostMapping.class);
        assertThat(mapping).isNotNull();
        assertThat(mapping.value()).containsExactly("/order/binding-facts/query");
        assertThat(VsoOrderService.class.getMethod("queryBindingFacts", BindingFactsQueryRequest.class))
                .isNotNull();
    }

    @Test
    @DisplayName("MATCHED 响应仅包含白名单字段，不回显 accountId 与客户资料")
    void shouldReturnWhitelistedFieldsOnly() throws Exception {
        when(bindingFactsQueryAppService.query(any())).thenReturn(BindingFactsResult.builder()
                .schemaVersion(1)
                .checkedAt(Instant.parse("2026-11-20T08:30:00.123Z"))
                .lookupStatus(BindingLookupStatus.MATCHED)
                .reasonCode(null)
                .facts(List.of(BindingFactsResult.Fact.builder()
                        .orderId("ORD-1")
                        .orderNo("SO202611200001")
                        .orderState(650)
                        .vin("LSVAA1234K2100001")
                        .matchedRoles(List.of("order_user", "buyer"))
                        .deliveryStatus(BindingDeliveryStatus.DELIVERED)
                        .deliveryRecordId("DR-1")
                        .actualDeliveryTime(Instant.parse("2026-11-20T06:30:15.000Z"))
                        .evidenceRefs(List.of(BindingFactsResult.EvidenceRef.builder()
                                .type(BindingEvidenceType.ORDER).id("ORD-1").version(3).build()))
                        .build()))
                .build());

        String body = mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"ACC-1001\",\"vin\":\"LSVAA1234K2100001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemaVersion").value(1))
                .andExpect(jsonPath("$.lookupStatus").value("MATCHED"))
                .andExpect(jsonPath("$.facts[0].orderId").value("ORD-1"))
                .andExpect(jsonPath("$.facts[0].matchedRoles[0]").value("order_user"))
                .andExpect(jsonPath("$.facts[0].deliveryStatus").value("DELIVERED"))
                .andExpect(jsonPath("$.facts[0].deliveryRecordId").value("DR-1"))
                .andExpect(jsonPath("$.facts[0].evidenceRefs[0].type").value("ORDER"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("accountId");
        assertThat(body).doesNotContain("canBind");
        assertThat(body).doesNotContain("isOwner");
        assertThat(body).doesNotContain("mobile");
        assertThat(body).doesNotContain("name");
    }

    @Test
    @DisplayName("NO_MATCH 以成功响应承载分类，空事实集合")
    void shouldReturnNoMatchAsSuccess() throws Exception {
        when(bindingFactsQueryAppService.query(any())).thenReturn(BindingFactsResult.builder()
                .schemaVersion(1)
                .checkedAt(Instant.parse("2026-11-20T08:30:00.000Z"))
                .lookupStatus(BindingLookupStatus.NO_MATCH)
                .reasonCode(net.hwyz.iov.cloud.otd.vso.api.enums.BindingReasonCode.NO_ACCOUNT_VIN_MATCH)
                .facts(List.of())
                .build());

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"ACC-1001\",\"vin\":\"LSVAA1234K2100001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lookupStatus").value("NO_MATCH"))
                .andExpect(jsonPath("$.reasonCode").value("NO_ACCOUNT_VIN_MATCH"))
                .andExpect(jsonPath("$.facts").isEmpty());
    }

    @Test
    @DisplayName("缺少账号/VIN 请求体校验失败返回 400 + 201001，且不触发查询")
    void shouldRejectMissingFields() throws Exception {
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"vin\":\"LSVAA1234K2100001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("201001"));

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"ACC-1001\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("201001"));

        verify(bindingFactsQueryAppService, never()).query(any());
    }

    @Test
    @DisplayName("查询条件透传，不做客户端自报角色/状态")
    void shouldPassOnlyAccountAndVin() throws Exception {
        when(bindingFactsQueryAppService.query(any())).thenReturn(BindingFactsResult.builder()
                .schemaVersion(1)
                .lookupStatus(BindingLookupStatus.NO_MATCH)
                .facts(List.of())
                .build());

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(BindingFactsQueryRequest.builder()
                                .accountId("ACC-1001")
                                .vin("LSVAA1234K2100001")
                                .build())))
                .andExpect(status().isOk());

        ArgumentCaptor<BindingFactsQuery> captor = ArgumentCaptor.forClass(BindingFactsQuery.class);
        verify(bindingFactsQueryAppService).query(captor.capture());
        assertThat(captor.getValue().getAccountId()).isEqualTo("ACC-1001");
        assertThat(captor.getValue().getVin()).isEqualTo("LSVAA1234K2100001");
    }

    @Test
    @DisplayName("技术失败不捕获成空成功：异常向上抛出由框架语义处理")
    void shouldNotSwallowTechnicalFailure() {
        when(bindingFactsQueryAppService.query(any()))
                .thenThrow(new IllegalStateException("db timeout"));

        Throwable thrown = org.assertj.core.api.Assertions.catchThrowable(() ->
                mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"ACC-1001\",\"vin\":\"LSVAA1234K2100001\"}")));
        assertThat(thrown).isNotNull();
        Throwable root = thrown;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        assertThat(root).isInstanceOf(IllegalStateException.class).hasMessage("db timeout");
    }

    @Test
    @DisplayName("响应时间字段为 ISO-8601（带 offset、固定 3 位毫秒）")
    void shouldSerializeTimeAsIso() throws Exception {
        when(bindingFactsQueryAppService.query(any())).thenReturn(BindingFactsResult.builder()
                .schemaVersion(1)
                .checkedAt(Instant.parse("2026-11-20T08:30:00.123Z"))
                .lookupStatus(BindingLookupStatus.NO_MATCH)
                .facts(List.of())
                .build());

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountId\":\"ACC-1001\",\"vin\":\"LSVAA1234K2100001\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("2026-11-20T08:30:00.123Z")));
    }
}
