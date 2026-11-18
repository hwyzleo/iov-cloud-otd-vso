package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * VSO 出站消息类型化配置（OTD-VSO-DSN-CR-012）
 * <p>
 * Topic 名称必须来自注册配置，禁止在代码中拼接推导。
 *
 * @author VSO Team
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "vso.outbox")
public class VsoOutboxProperties {

    /**
     * 聚合类型
     */
    @NotBlank
    private String aggregateType = "ORDER";

    /**
     * Topic 注册表
     */
    private Topics topics = new Topics();

    /**
     * 中继配置
     */
    private Relay relay = new Relay();

    /**
     * Topic 注册表
     */
    @Data
    public static class Topics {

        /**
         * 发运申请事实 Topic
         */
        @NotBlank
        private String transportApplied = "vso.transport.applied";
    }

    /**
     * 中继参数
     */
    @Data
    public static class Relay {

        /**
         * 是否启用中继
         */
        private boolean enabled = true;

        /**
         * 扫描间隔（毫秒）
         */
        @Min(1000)
        private long scanIntervalMs = 10000;

        /**
         * 单批条数
         */
        @Min(1)
        private int batchSize = 100;

        /**
         * 最大重试次数（超过置 DEAD）
         */
        @Min(1)
        private int maxRetry = 10;

        /**
         * 基础退避（毫秒），按 2^n 递增并封顶 {@link #maxBackoffMs}
         */
        @Min(1000)
        private long baseBackoffMs = 5000;

        /**
         * 退避上限（毫秒）
         */
        @Min(1000)
        private long maxBackoffMs = 300000;
    }
}
