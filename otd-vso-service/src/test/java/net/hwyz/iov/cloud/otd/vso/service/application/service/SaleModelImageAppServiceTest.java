package net.hwyz.iov.cloud.otd.vso.service.application.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import net.hwyz.iov.cloud.edd.oss.api.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import net.hwyz.iov.cloud.edd.oss.api.vo.request.GenerateImageUploadRequest;
import net.hwyz.iov.cloud.edd.oss.api.vo.response.ImageObjectResponse;
import net.hwyz.iov.cloud.edd.oss.api.vo.response.ImageUploadResponse;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageResultVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageTicketVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GenerateSaleModelImageUploadCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadTicketInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * SaleModelImageAppService 单元测试（OTD-VSO-DSN-CR-003 §8）
 * <p>
 * 覆盖：Redis 绑定写入与 TTL、单次使用删除、失败保留重试、Redis 不可用拒绝、
 * 重复确认/操作人不一致/过期返回 201055、OSS 失败返回 201056、日志不泄露预签名地址与凭证。
 *
 * @author VSO Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaleModelImageAppService - 上传凭证绑定与异常映射")
class SaleModelImageAppServiceTest {

    private static final String OBJECT_ID = "OBJ20261112000001";
    private static final String OPERATOR_ID = "1001";
    private static final String OTHER_OPERATOR_ID = "2002";
    private static final String BIND_KEY = "vso:image:upload:" + OBJECT_ID;
    private static final String PRESIGNED_URL = "https://cos.example.com/upload?signature=SECRET";
    private static final long DEFAULT_TTL_SECONDS = 1800L;

    @Mock
    private ImageService imageService;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SaleModelImageAppService saleModelImageAppService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    private GenerateSaleModelImageUploadCmd cmd(String filename, String contentType, Long size) {
        return GenerateSaleModelImageUploadCmd.builder()
                .originalFilename(filename)
                .contentType(contentType)
                .size(size)
                .operatorId(OPERATOR_ID)
                .build();
    }

    private ImageUploadResponse uploadResponse(long expireAt) {
        return ImageUploadResponse.builder()
                .objectId(OBJECT_ID)
                .objectKey("image/2026/11/12/OBJ20261112000001.png")
                .uploadUrl(PRESIGNED_URL)
                .httpMethod("PUT")
                .contentType("image/png")
                .expiresAt(expireAt)
                .build();
    }

    private ImageObjectResponse confirmedResponse(String status, String accessUrl) {
        return ImageObjectResponse.builder()
                .objectId(OBJECT_ID)
                .accessUrl(accessUrl)
                .actualContentType("image/png")
                .actualSize(2048L)
                .status(status)
                .build();
    }

    @Test
    @DisplayName("申请凭证：返回 OSS 字段与 32 位凭证，并按配置 TTL 写入操作人绑定")
    void 申请凭证写入绑定() {
        long expireAt = System.currentTimeMillis() + 15 * 60 * 1000;
        when(imageService.generateUpload(any(GenerateImageUploadRequest.class))).thenReturn(uploadResponse(expireAt));
        ReflectionTestUtils.setField(saleModelImageAppService, "bindTtlSeconds", 900L);

        SaleModelImageTicketVo vo = saleModelImageAppService.generateTicket(cmd("车型.png", "image/png", 2048L));

        assertEquals(OBJECT_ID, vo.getObjectId());
        assertEquals(PRESIGNED_URL, vo.getUploadUrl());
        assertEquals("PUT", vo.getHttpMethod());
        assertEquals("image/png", vo.getContentType());
        assertEquals(expireAt, vo.getExpiresAt());
        assertNotNull(vo.getUploadToken());
        assertEquals(32, vo.getUploadToken().length(), "上传凭证应为 32 位随机串");

        ArgumentCaptor<GenerateImageUploadRequest> requestCaptor =
                ArgumentCaptor.forClass(GenerateImageUploadRequest.class);
        verify(imageService).generateUpload(requestCaptor.capture());
        assertEquals("车型.png", requestCaptor.getValue().getOriginalFilename());
        assertEquals("image/png", requestCaptor.getValue().getContentType());
        assertEquals(2048L, requestCaptor.getValue().getSize());

        verify(valueOperations).set(BIND_KEY, OPERATOR_ID + "|" + expireAt, 900L, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("申请凭证：默认绑定 TTL 为 1800 秒")
    void 申请凭证默认TTL() {
        when(imageService.generateUpload(any(GenerateImageUploadRequest.class)))
                .thenReturn(uploadResponse(System.currentTimeMillis() + 60_000L));

        saleModelImageAppService.generateTicket(cmd("车型.png", "image/png", 2048L));

        verify(valueOperations).set(eq(BIND_KEY), anyString(), eq(DEFAULT_TTL_SECONDS), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("申请凭证：非法 MIME、空文件名、超长文件名、非法大小均在调用 OSS 前拒绝")
    void 申请凭证前置校验() {
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("a.gif", "image/gif", 1024L)));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("   ", "image/png", 1024L)));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("a".repeat(256) + ".png", "image/png", 1024L)));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("a.png", "image/png", 0L)));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("a.png", "image/png", 10485761L)));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(GenerateSaleModelImageUploadCmd.builder()
                        .originalFilename("a.png").contentType("image/png").size(1024L).operatorId("").build()));

        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("申请凭证：Redis 不可用时拒绝申请（fail-closed，不返回可用凭证）")
    void 申请凭证Redis不可用拒绝() {
        when(imageService.generateUpload(any(GenerateImageUploadRequest.class)))
                .thenReturn(uploadResponse(System.currentTimeMillis() + 60_000L));
        doThrow(new IllegalStateException("redis down"))
                .when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("车型.png", "image/png", 2048L)));
    }

    @Test
    @DisplayName("申请凭证：OSS 调用异常统一映射为 IMAGE_UPLOAD_FAILED")
    void 申请凭证OSS异常映射() {
        when(imageService.generateUpload(any(GenerateImageUploadRequest.class)))
                .thenThrow(new IllegalStateException("图片上传服务不可用"));

        ImageUploadFailedException exception = assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.generateTicket(cmd("车型.png", "image/png", 2048L)));

        assertEquals("201056", exception.getErrorCode().getCode());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("完成确认：绑定校验通过后调用 OSS，返回 accessUrl 并删除绑定键")
    void 完成确认成功并消费绑定() {
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() + 60_000L));
        when(imageService.completeUpload(OBJECT_ID))
                .thenReturn(confirmedResponse("UPLOADED", "https://cdn.example.com/OBJ20261112000001.png"));

        SaleModelImageResultVo vo = saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID);

        assertEquals(OBJECT_ID, vo.getObjectId());
        assertEquals("https://cdn.example.com/OBJ20261112000001.png", vo.getAccessUrl());
        assertEquals("image/png", vo.getContentType());
        assertEquals(2048L, vo.getSize());
        assertEquals("UPLOADED", vo.getStatus());
        verify(redisTemplate).delete(BIND_KEY);
    }

    @Test
    @DisplayName("完成确认：绑定缺失（凭证无效/已消费）返回 201055 且不调用 OSS")
    void 完成确认绑定缺失() {
        when(valueOperations.get(BIND_KEY)).thenReturn(null);

        ImageUploadTicketInvalidException exception = assertThrows(ImageUploadTicketInvalidException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        assertEquals("201055", exception.getErrorCode().getCode());
        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("完成确认：重复提交（绑定已消费）返回 201055")
    void 完成确认重复提交() {
        when(valueOperations.get(BIND_KEY))
                .thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() + 60_000L), (String) null);
        when(imageService.completeUpload(OBJECT_ID))
                .thenReturn(confirmedResponse("UPLOADED", "https://cdn.example.com/OBJ20261112000001.png"));

        saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID);

        assertThrows(ImageUploadTicketInvalidException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));
    }

    @Test
    @DisplayName("完成确认：操作人不一致返回 201055 且不调用 OSS")
    void 完成确认操作人不一致() {
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() + 60_000L));

        ImageUploadTicketInvalidException exception = assertThrows(ImageUploadTicketInvalidException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OTHER_OPERATOR_ID));

        assertEquals("201055", exception.getErrorCode().getCode());
        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("完成确认：绑定已过期返回 201055 且不调用 OSS")
    void 完成确认凭证过期() {
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() - 1_000L));

        ImageUploadTicketInvalidException exception = assertThrows(ImageUploadTicketInvalidException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        assertEquals("201055", exception.getErrorCode().getCode());
        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("完成确认：OSS 失败返回 201056 且保留绑定键以允许重试")
    void 完成确认失败保留绑定() {
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() + 60_000L));
        when(imageService.completeUpload(OBJECT_ID)).thenThrow(new IllegalStateException("图片上传服务不可用"));

        ImageUploadFailedException exception = assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        assertEquals("201056", exception.getErrorCode().getCode());
        verify(redisTemplate, never()).delete(BIND_KEY);
    }

    @Test
    @DisplayName("完成确认：对象状态未达 UPLOADED 或缺少 accessUrl 返回 201056，不返回半成品地址")
    void 完成确认状态未达上传完成() {
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + (System.currentTimeMillis() + 60_000L));
        when(imageService.completeUpload(OBJECT_ID)).thenReturn(confirmedResponse("PENDING", null));

        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        when(imageService.completeUpload(OBJECT_ID)).thenReturn(confirmedResponse("UPLOADED", " "));
        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        verify(redisTemplate, never()).delete(BIND_KEY);
    }

    @Test
    @DisplayName("完成确认：Redis 不可用时拒绝（不得无绑定降级调用 OSS）")
    void 完成确认Redis不可用拒绝() {
        when(valueOperations.get(BIND_KEY)).thenThrow(new IllegalStateException("redis down"));

        assertThrows(ImageUploadFailedException.class,
                () -> saleModelImageAppService.complete(OBJECT_ID, "token-32", OPERATOR_ID));

        verifyNoInteractions(imageService);
    }

    @Test
    @DisplayName("日志不得输出预签名地址与上传凭证")
    void 日志不泄露预签名地址与凭证() {
        long expireAt = System.currentTimeMillis() + 60_000L;
        when(imageService.generateUpload(any(GenerateImageUploadRequest.class))).thenReturn(uploadResponse(expireAt));
        when(valueOperations.get(BIND_KEY)).thenReturn(OPERATOR_ID + "|" + expireAt);
        when(imageService.completeUpload(OBJECT_ID))
                .thenReturn(confirmedResponse("UPLOADED", "https://cdn.example.com/OBJ20261112000001.png"));

        String loggerName = SaleModelImageAppService.class.getName();
        List<String> messages = new ArrayList<>();
        AbstractAppender appender = new AbstractAppender("cr003-log-capture", null,
                PatternLayout.createDefaultLayout(), false, null) {
            @Override
            public void append(LogEvent event) {
                messages.add(event.getMessage().getFormattedMessage());
            }
        };
        appender.start();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        LoggerConfig loggerConfig = new LoggerConfig(loggerName, Level.INFO, true);
        loggerConfig.addAppender(appender, Level.INFO, null);
        configuration.addLogger(loggerName, loggerConfig);
        context.updateLoggers();
        String uploadToken;
        try {
            SaleModelImageTicketVo ticket = saleModelImageAppService.generateTicket(cmd("车型.png", "image/png", 2048L));
            uploadToken = ticket.getUploadToken();
            saleModelImageAppService.complete(OBJECT_ID, uploadToken, OPERATOR_ID);
        } finally {
            loggerConfig.removeAppender(appender.getName());
            appender.stop();
            configuration.addLogger(loggerName, new LoggerConfig(loggerName, Level.ERROR, true));
            context.updateLoggers();
        }

        assertFalse(messages.isEmpty(), "应捕获到应用服务日志");
        assertTrue(messages.stream().noneMatch(message -> message.contains(PRESIGNED_URL)),
                "日志不得输出预签名地址");
        assertTrue(messages.stream().noneMatch(message -> message.contains(uploadToken)),
                "日志不得输出上传凭证");
    }
}
