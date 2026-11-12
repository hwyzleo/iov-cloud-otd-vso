package net.hwyz.iov.cloud.otd.vso.service.application.service;

import cn.hutool.core.util.IdUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.edd.oss.api.service.ImageService;
import net.hwyz.iov.cloud.edd.oss.api.vo.request.GenerateImageUploadRequest;
import net.hwyz.iov.cloud.edd.oss.api.vo.response.ImageObjectResponse;
import net.hwyz.iov.cloud.edd.oss.api.vo.response.ImageUploadResponse;
import net.hwyz.iov.cloud.framework.common.util.StrUtil;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageResultVo;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.SaleModelImageTicketVo;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.GenerateSaleModelImageUploadCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadFailedException;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.ImageUploadTicketInvalidException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 销售车型图片上传应用服务（OTD-VSO-DSN-CR-003）
 * <p>
 * VSO 作为 EDD-OSS 的场景化薄封装：只签发预签名参数并做完成确认，不中转图片二进制、
 * 不持有 COS 密钥、不保存 objectId 与销售车型的关联。上传凭证与操作人绑定在 Redis，
 * 完成确认前必须校验归属；Redis 不可用时 fail-closed，禁止无绑定降级。
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaleModelImageAppService {

    /**
     * Redis 上传凭证绑定键前缀，完整键：vso:image:upload:{objectId}
     */
    private static final String BIND_KEY_PREFIX = "vso:image:upload:";

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;

    private static final long MAX_FILENAME_LENGTH = 255;

    private static final String STATUS_UPLOADED = "UPLOADED";

    private static final String DEFAULT_HTTP_METHOD = "PUT";

    private static final String BIND_VALUE_SEPARATOR = "|";

    private final ImageService imageService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 上传凭证绑定有效期（秒），不短于预签名有效期
     */
    @Value("${vso.image.upload.bind-ttl-seconds:1800}")
    private long bindTtlSeconds = 1800;

    /**
     * 申请上传凭证：前置校验 → 调用 EDD-OSS 创建上传任务 → 写入操作人归属绑定
     *
     * @param cmd 申请命令
     * @return 上传凭证（不含 objectKey）
     */
    public SaleModelImageTicketVo generateTicket(GenerateSaleModelImageUploadCmd cmd) {
        if (cmd == null || StrUtil.isBlank(cmd.getOperatorId())) {
            throw new ImageUploadFailedException("未获取到当前登录操作人，拒绝申请图片上传凭证");
        }
        String originalFilename = StrUtil.trim(cmd.getOriginalFilename());
        String contentType = normalizeContentType(cmd.getContentType());
        validateRequest(originalFilename, contentType, cmd.getSize());

        ImageUploadResponse upload = requestUpload(originalFilename, contentType, cmd.getSize(), cmd.getOperatorId());
        if (StrUtil.isBlank(upload.getObjectId()) || StrUtil.isBlank(upload.getUploadUrl())) {
            log.error("[图片上传] OSS 未返回有效上传任务 operatorId[{}] filename[{}] contentType[{}] size[{}]",
                    cmd.getOperatorId(), originalFilename, contentType, cmd.getSize());
            throw new ImageUploadFailedException("申请图片上传凭证失败：对象存储未返回有效上传任务");
        }

        String uploadToken = IdUtil.fastSimpleUUID();
        bindOperator(upload.getObjectId(), cmd.getOperatorId(), upload.getExpiresAt());

        SaleModelImageTicketVo vo = new SaleModelImageTicketVo();
        vo.setObjectId(upload.getObjectId());
        vo.setUploadUrl(upload.getUploadUrl());
        vo.setHttpMethod(StrUtil.isBlank(upload.getHttpMethod()) ? DEFAULT_HTTP_METHOD : upload.getHttpMethod());
        vo.setContentType(StrUtil.isBlank(upload.getContentType()) ? contentType : upload.getContentType());
        vo.setExpiresAt(upload.getExpiresAt());
        vo.setUploadToken(uploadToken);
        log.info("[图片上传] 上传凭证签发成功 objectId[{}] operatorId[{}] filename[{}] contentType[{}] size[{}]",
                upload.getObjectId(), cmd.getOperatorId(), originalFilename, contentType, cmd.getSize());
        return vo;
    }

    /**
     * 完成确认：校验凭证绑定（对象/操作人/有效期）→ 调用 EDD-OSS 完成确认 → 成功后消费绑定
     *
     * @param objectId    上传任务标识
     * @param uploadToken 上传凭证
     * @param operatorId  当前操作人 ID（来自安全上下文）
     * @return 已完成确认的图片对象信息
     */
    public SaleModelImageResultVo complete(String objectId, String uploadToken, String operatorId) {
        if (StrUtil.isBlank(operatorId)) {
            throw new ImageUploadTicketInvalidException("未获取到当前登录操作人，拒绝完成图片上传确认");
        }
        if (StrUtil.isBlank(objectId) || StrUtil.isBlank(uploadToken)) {
            throw new ImageUploadTicketInvalidException("上传凭证缺失");
        }
        String bindKey = BIND_KEY_PREFIX + objectId;
        verifyBinding(bindKey, objectId, operatorId);

        ImageObjectResponse confirmed = requestComplete(objectId, operatorId);
        if (confirmed == null || StrUtil.isBlank(confirmed.getAccessUrl())
                || !STATUS_UPLOADED.equalsIgnoreCase(confirmed.getStatus())) {
            log.error("[图片上传] 完成确认未达 UPLOADED objectId[{}] operatorId[{}] status[{}]",
                    objectId, operatorId, confirmed == null ? null : confirmed.getStatus());
            throw new ImageUploadFailedException("完成图片上传确认失败：对象状态未达到 UPLOADED");
        }

        consumeBinding(bindKey, objectId, operatorId);

        SaleModelImageResultVo vo = new SaleModelImageResultVo();
        vo.setObjectId(StrUtil.isBlank(confirmed.getObjectId()) ? objectId : confirmed.getObjectId());
        vo.setAccessUrl(confirmed.getAccessUrl());
        vo.setContentType(confirmed.getActualContentType());
        vo.setSize(confirmed.getActualSize());
        vo.setStatus(confirmed.getStatus());
        log.info("[图片上传] 完成确认成功 objectId[{}] operatorId[{}] actualContentType[{}] actualSize[{}]",
                objectId, operatorId, confirmed.getActualContentType(), confirmed.getActualSize());
        return vo;
    }

    /**
     * 调用 EDD-OSS 创建上传任务；异常统一转 IMAGE_UPLOAD_FAILED，不回显内部堆栈与预签名地址
     */
    private ImageUploadResponse requestUpload(String originalFilename, String contentType, Long size, String operatorId) {
        try {
            return imageService.generateUpload(GenerateImageUploadRequest.builder()
                    .originalFilename(originalFilename)
                    .contentType(contentType)
                    .size(size)
                    .build());
        } catch (RuntimeException exception) {
            log.error("[图片上传] 调用 OSS 申请上传凭证失败 operatorId[{}] filename[{}] contentType[{}] size[{}] ossStatus[{}]",
                    operatorId, originalFilename, contentType, size, ossStatus(exception));
            throw new ImageUploadFailedException("申请图片上传凭证失败");
        }
    }

    /**
     * 调用 EDD-OSS 完成确认；失败时保留绑定键以允许同一 objectId 重试
     */
    private ImageObjectResponse requestComplete(String objectId, String operatorId) {
        try {
            return imageService.completeUpload(objectId);
        } catch (RuntimeException exception) {
            log.error("[图片上传] 调用 OSS 完成确认失败 objectId[{}] operatorId[{}] ossStatus[{}]",
                    objectId, operatorId, ossStatus(exception));
            throw new ImageUploadFailedException("完成图片上传确认失败");
        }
    }

    /**
     * 写入操作人归属绑定；Redis 异常时拒绝申请，禁止无绑定降级
     */
    private void bindOperator(String objectId, String operatorId, Long expireAt) {
        String bindValue = operatorId + BIND_VALUE_SEPARATOR + expireAt;
        try {
            redisTemplate.opsForValue().set(BIND_KEY_PREFIX + objectId, bindValue, bindTtlSeconds, TimeUnit.SECONDS);
        } catch (RuntimeException exception) {
            log.error("[图片上传] 上传凭证绑定失败 objectId[{}] operatorId[{}]", objectId, operatorId);
            throw new ImageUploadFailedException("图片上传凭证绑定失败");
        }
    }

    /**
     * 校验绑定：凭证存在、对象一致、操作人一致、未过期
     */
    private void verifyBinding(String bindKey, String objectId, String operatorId) {
        String bindValue;
        try {
            bindValue = redisTemplate.opsForValue().get(bindKey);
        } catch (RuntimeException exception) {
            log.error("[图片上传] 读取上传凭证绑定失败 objectId[{}] operatorId[{}]", objectId, operatorId);
            throw new ImageUploadFailedException("图片上传凭证校验失败");
        }
        if (StrUtil.isBlank(bindValue)) {
            throw new ImageUploadTicketInvalidException("上传凭证无效、已过期或已使用");
        }
        int separatorIndex = bindValue.lastIndexOf(BIND_VALUE_SEPARATOR);
        if (separatorIndex <= 0) {
            throw new ImageUploadTicketInvalidException("上传凭证无效、已过期或已使用");
        }
        String boundOperatorId = bindValue.substring(0, separatorIndex);
        long expireAt = parseExpireAt(bindValue.substring(separatorIndex + BIND_VALUE_SEPARATOR.length()));
        if (!operatorId.equals(boundOperatorId)) {
            log.warn("[图片上传] 上传凭证操作人不一致 objectId[{}] operatorId[{}]", objectId, operatorId);
            throw new ImageUploadTicketInvalidException("上传凭证与当前操作人不一致");
        }
        if (System.currentTimeMillis() > expireAt) {
            throw new ImageUploadTicketInvalidException("上传凭证已过期");
        }
    }

    /**
     * 成功后消费绑定键（单次使用）
     */
    private void consumeBinding(String bindKey, String objectId, String operatorId) {
        try {
            redisTemplate.delete(bindKey);
        } catch (RuntimeException exception) {
            // 对象已确认成功，不回滚结果；绑定键残留由 TTL 兜底
            log.warn("[图片上传] 上传凭证绑定删除失败 objectId[{}] operatorId[{}]", objectId, operatorId);
        }
    }

    private long parseExpireAt(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ImageUploadTicketInvalidException("上传凭证无效、已过期或已使用");
        }
    }

    private String normalizeContentType(String contentType) {
        return StrUtil.isBlank(contentType) ? null : contentType.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 服务端兜底校验（Controller 侧已由 Bean Validation 前置拦截），不通过不得调用 OSS
     */
    private void validateRequest(String originalFilename, String contentType, Long size) {
        if (StrUtil.isBlank(originalFilename)) {
            throw new ImageUploadFailedException("原始文件名不能为空");
        }
        if (originalFilename.length() > MAX_FILENAME_LENGTH) {
            throw new ImageUploadFailedException("原始文件名长度不能超过 255 字符");
        }
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ImageUploadFailedException("仅支持 image/jpeg、image/png、image/webp");
        }
        if (size == null || size < 1 || size > MAX_IMAGE_SIZE_BYTES) {
            throw new ImageUploadFailedException("图片大小必须在 1 字节至 10 MiB 之间");
        }
    }

    /**
     * 提取 OSS 返回码用于日志，仅记录状态码，不记录响应体与预签名地址
     */
    private Integer ossStatus(Throwable throwable) {
        Throwable current = throwable;
        for (int depth = 0; current != null && depth < 5; depth++) {
            if (current instanceof FeignException feignException) {
                return feignException.status();
            }
            current = current.getCause();
        }
        return null;
    }
}
