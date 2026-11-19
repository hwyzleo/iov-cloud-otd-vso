package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.framework.web.handler.GlobalExceptionHandler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.InitiateContractSignCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.ContractSignResult;
import net.hwyz.iov.cloud.otd.vso.service.application.service.ContractAppService;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ContractNotExistException;
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

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * C 端购车合同接口契约测试（OTD-VSO-DSN-CR-014 US-032 / §26.8）
 * <p>
 * 覆盖：发起签署与查询合同路由、订单号透传、错误码 201061、时间字段 ISO-8601 固定 3 位毫秒。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("C 端购车合同 - 契约与时间形态")
class MobileContractControllerTest {

    private static final String BASE = "/api/mobile/vso/v1";

    @Mock
    private ContractAppService contractAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MobileContractController controller = new MobileContractController(contractAppService);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
                .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("发起合同签署：透传 orderNo，返回 signNo 与 SIGNING")
    void 发起签署契约() throws Exception {
        when(contractAppService.initiateSign(any())).thenReturn(ContractSignResult.builder()
                .contractNo("CT1")
                .contractType("VEHICLE_PURCHASE")
                .contractStatus("SIGNING")
                .signNo("SIGN1")
                .signUrl("mock://esign/sign/SIGN1")
                .fileUrl("mock://esign/contract/1")
                .generateTime(Instant.parse("2026-11-19T06:07:00.123Z"))
                .build());

        mockMvc.perform(post(BASE + "/order/action/initiateContractSign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"SO001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000000"))
                .andExpect(jsonPath("$.data.contractNo").value("CT1"))
                .andExpect(jsonPath("$.data.contractStatus").value("SIGNING"))
                .andExpect(jsonPath("$.data.signNo").value("SIGN1"))
                .andExpect(jsonPath("$.data.generateTime").value("2026-11-19T06:07:00.123Z"));

        ArgumentCaptor<InitiateContractSignCmd> captor = ArgumentCaptor.forClass(InitiateContractSignCmd.class);
        verify(contractAppService).initiateSign(captor.capture());
        assertEquals("SO001", captor.getValue().getOrderNo());
    }

    @Test
    @DisplayName("查询合同：时间字段为 ISO-8601 固定 3 位毫秒")
    void 查询合同时间ISO() throws Exception {
        when(contractAppService.getContract(any(), any())).thenReturn(ContractDetailResult.builder()
                .contractNo("CT1")
                .contractType("VEHICLE_PURCHASE")
                .contractStatus("SIGNED")
                .signNo("SIGN1")
                .fileUrl("mock://esign/contract/1")
                .generateTime(Instant.parse("2026-11-19T06:07:00.123Z"))
                .signTime(Instant.parse("2026-11-19T06:08:01.456Z"))
                .build());

        mockMvc.perform(get(BASE + "/order/SO001/contract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contractStatus").value("SIGNED"))
                .andExpect(jsonPath("$.data.generateTime").value("2026-11-19T06:07:00.123Z"))
                .andExpect(jsonPath("$.data.signTime").value("2026-11-19T06:08:01.456Z"))
                .andExpect(jsonPath("$.data.signUrl").doesNotExist());
    }

    @Test
    @DisplayName("无合同：返回业务错误码 201061，Controller 不吞异常")
    void 无合同错误码() throws Exception {
        when(contractAppService.getContract(any(), any())).thenThrow(new ContractNotExistException("SO001"));

        mockMvc.perform(get(BASE + "/order/SO001/contract"))
                .andExpect(jsonPath("$.code").value("201061"));
    }
}
