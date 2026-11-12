package net.hwyz.iov.cloud.otd.vso.service.adapter.web.controller.mpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.hwyz.iov.cloud.framework.common.exception.BusinessException;
import net.hwyz.iov.cloud.framework.web.context.SecurityContextHolder;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageResultVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageTicketVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GenerateSaleModelImageUploadCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.service.SaleModelImageAppService;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadTicketInvalidException;
import org.junit.jupiter.api.AfterEach;
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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MPT 销售车型图片上传契约测试（OTD-VSO-DSN-CR-003 §8）
 * <p>
 * 覆盖：参数校验前置拒绝（不调 OSS/应用服务）、操作人来自安全上下文而非请求头、
 * 响应不含 objectKey、凭证校验失败返回 201055、OSS 失败映射 201056 且不被 Controller 吞掉。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MPT 销售车型图片上传 - 参数校验/操作人/错误码")
class MptSaleModelImageControllerTest {

    private static final String BASE = "/api/mpt/saleModel/v1/upload";
    private static final String OPERATOR_ID = "1001";

    @Mock
    private SaleModelImageAppService saleModelImageAppService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setUserId(OPERATOR_ID);
        MptSaleModelImageController controller = new MptSaleModelImageController(saleModelImageAppService);
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setLocalMap(new HashMap<>());
    }

    private String ticketBody(String filename, String contentType, Long size) {
        return "{\"originalFilename\":\"" + filename + "\",\"contentType\":\"" + contentType
                + "\",\"size\":" + size + "}";
    }

    @Test
    @DisplayName("申请凭证：文件名缺失/超长、MIME 非白名单、大小越界均返回 400 且不触达应用服务")
    void 申请凭证参数校验() throws Exception {
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("", "image/png", 1024L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("a".repeat(256) + ".png", "image/png", 1024L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("a.gif", "image/gif", 1024L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("a.png", "image/png", 0L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("a.png", "image/png", 10485761L)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contentType\":\"image/png\",\"size\":1024}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(saleModelImageAppService);
    }

    @Test
    @DisplayName("申请凭证：成功返回凭证字段且响应不含 objectKey")
    void 申请凭证成功() throws Exception {
        SaleModelImageTicketVo vo = new SaleModelImageTicketVo();
        vo.setObjectId("OBJ1");
        vo.setUploadUrl("https://cos.example.com/put");
        vo.setHttpMethod("PUT");
        vo.setContentType("image/png");
        vo.setExpiresAt(1_700_000_000_000L);
        vo.setUploadToken("0123456789abcdef0123456789abcdef");
        when(saleModelImageAppService.generateTicket(any(GenerateSaleModelImageUploadCmd.class))).thenReturn(vo);

        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .content(ticketBody("车型.png", "image/png", 2048L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.objectId").value("OBJ1"))
                .andExpect(jsonPath("$.data.uploadUrl").value("https://cos.example.com/put"))
                .andExpect(jsonPath("$.data.httpMethod").value("PUT"))
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andExpect(jsonPath("$.data.expiresAt").value(1_700_000_000_000L))
                .andExpect(jsonPath("$.data.uploadToken").value("0123456789abcdef0123456789abcdef"))
                .andExpect(jsonPath("$.data.objectKey").doesNotExist());
    }

    @Test
    @DisplayName("操作人一律取安全上下文，忽略 X-Operator-Id 请求头")
    void 操作人来自安全上下文() throws Exception {
        when(saleModelImageAppService.generateTicket(any(GenerateSaleModelImageUploadCmd.class)))
                .thenReturn(new SaleModelImageTicketVo());

        mockMvc.perform(post(BASE + "/tickets").contentType(MediaType.APPLICATION_JSON)
                        .header("X-Operator-Id", "9999")
                        .content(ticketBody("车型.png", "image/png", 2048L)))
                .andExpect(status().isOk());

        ArgumentCaptor<GenerateSaleModelImageUploadCmd> captor =
                ArgumentCaptor.forClass(GenerateSaleModelImageUploadCmd.class);
        verify(saleModelImageAppService).generateTicket(captor.capture());
        assertEquals(OPERATOR_ID, captor.getValue().getOperatorId());
    }

    @Test
    @DisplayName("完成确认：uploadToken 缺失返回 400 且不触达应用服务")
    void 完成确认凭证缺失() throws Exception {
        mockMvc.perform(post(BASE + "/tickets/OBJ1/complete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadToken\":\"  \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(saleModelImageAppService);
    }

    @Test
    @DisplayName("完成确认：成功返回 accessUrl 等字段，objectId/凭证/操作人按契约传递")
    void 完成确认成功() throws Exception {
        SaleModelImageResultVo vo = new SaleModelImageResultVo();
        vo.setObjectId("OBJ1");
        vo.setAccessUrl("https://cdn.example.com/OBJ1.png");
        vo.setContentType("image/png");
        vo.setSize(2048L);
        vo.setStatus("UPLOADED");
        when(saleModelImageAppService.complete(eq("OBJ1"), anyString(), eq(OPERATOR_ID))).thenReturn(vo);

        mockMvc.perform(post(BASE + "/tickets/OBJ1/complete").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadToken\":\"0123456789abcdef0123456789abcdef\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.objectId").value("OBJ1"))
                .andExpect(jsonPath("$.data.accessUrl").value("https://cdn.example.com/OBJ1.png"))
                .andExpect(jsonPath("$.data.contentType").value("image/png"))
                .andExpect(jsonPath("$.data.size").value(2048))
                .andExpect(jsonPath("$.data.status").value("UPLOADED"));

        verify(saleModelImageAppService).complete("OBJ1", "0123456789abcdef0123456789abcdef", OPERATOR_ID);
    }

    @Test
    @DisplayName("凭证无效/操作人不一致：业务异常 201055 向上抛出，不被 Controller 吞掉")
    void 凭证无效异常不吞() {
        when(saleModelImageAppService.complete(anyString(), anyString(), anyString()))
                .thenThrow(new ImageUploadTicketInvalidException("上传凭证与当前操作人不一致"));

        assertEquals("201055", assertBusinessErrorCode("201055"));
    }

    @Test
    @DisplayName("OSS 调用失败：业务异常 201056 向上抛出，不返回半成品地址")
    void 上传失败异常不吞() {
        when(saleModelImageAppService.complete(anyString(), anyString(), anyString()))
                .thenThrow(new ImageUploadFailedException("完成图片上传确认失败"));

        assertEquals("201056", assertBusinessErrorCode("201056"));
    }

    private String assertBusinessErrorCode(String expectedCode) {
        Exception exception = assertThrows(Exception.class, () -> mockMvc.perform(
                post(BASE + "/tickets/OBJ1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"uploadToken\":\"0123456789abcdef0123456789abcdef\"}")).andReturn());
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof BusinessException businessException) {
                assertTrue(businessException.getErrorCode().getCode().startsWith("201"));
                return businessException.getErrorCode().getCode();
            }
            cause = cause.getCause();
        }
        throw new AssertionError("未找到业务异常，期望错误码：" + expectedCode);
    }
}
