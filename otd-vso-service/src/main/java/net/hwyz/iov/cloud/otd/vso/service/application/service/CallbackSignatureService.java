package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 支付回调签名验证服务
 * <p>
 * 签名算法：HMAC-SHA256
 * 签名内容：按字段字典序排列，用 & 连接
 * 防重放：timestamp ±5分钟，nonce Redis SET TTL 5分钟
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallbackSignatureService {

    private final StringRedisTemplate redisTemplate;

    @Value("${vso.payment.callback.signature-secret:hwyz1234567890abcdef}")
    private String signatureSecret;

    private static final String NONCE_KEY_PREFIX = "vso:callback:nonce:";
    private static final long TIMESTAMP_TOLERANCE_SECONDS = 300;
    private static final long NONCE_TTL_SECONDS = 300;

    /**
     * 验证回调签名
     *
     * @param signature  签名（来自 X-Signature header）
     * @param timestamp  时间戳（Unix秒）
     * @param nonce      随机串
     * @param signData   签名数据（amount, orderId, paySeq, status）
     * @return 是否验证通过
     */
    public boolean verifySignature(String signature, Long timestamp, String nonce, Map<String, String> signData) {
        if (signature == null || timestamp == null || nonce == null) {
            log.warn("签名验证失败：必要参数为空");
            return false;
        }

        if (!verifyTimestamp(timestamp)) {
            log.warn("签名验证失败：timestamp 超时或无效 timestamp={}", timestamp);
            return false;
        }

        if (!verifyAndStoreNonce(nonce)) {
            log.warn("签名验证失败：nonce 已使用或无效 nonce={}", nonce);
            return false;
        }

        String content = buildSignatureContent(signData, nonce, timestamp);
        String expectedSignature = calculateHmacSha256(content, signatureSecret);

        if (!signature.equals(expectedSignature)) {
            log.warn("签名验证失败：签名不匹配 expected={}, actual={}", expectedSignature, signature);
            return false;
        }

        log.debug("签名验证成功");
        return true;
    }

    private boolean verifyTimestamp(Long timestamp) {
        long now = Instant.now().getEpochSecond();
        long diff = Math.abs(now - timestamp);
        return diff <= TIMESTAMP_TOLERANCE_SECONDS;
    }

    private boolean verifyAndStoreNonce(String nonce) {
        String nonceKey = NONCE_KEY_PREFIX + nonce;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(nonceKey, "1", NONCE_TTL_SECONDS, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    private String buildSignatureContent(Map<String, String> signData, String nonce, Long timestamp) {
        TreeMap<String, String> sortedMap = new TreeMap<>(signData);
        sortedMap.put("nonce", nonce);
        sortedMap.put("timestamp", String.valueOf(timestamp));

        StringBuilder sb = new StringBuilder();
        sortedMap.forEach((key, value) -> {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key).append("=").append(value != null ? value : "");
        });
        return sb.toString();
    }

    private String calculateHmacSha256(String content, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacBytes);
        } catch (Exception e) {
            log.error("HMAC-SHA256 计算失败", e);
            return "";
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
