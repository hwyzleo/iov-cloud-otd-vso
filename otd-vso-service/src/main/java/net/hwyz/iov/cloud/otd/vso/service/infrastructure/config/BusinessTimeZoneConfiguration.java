package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import net.hwyz.iov.cloud.framework.web.jackson.JacksonProperties;
import net.hwyz.iov.cloud.otd.vso.service.common.time.BusinessTimeZone;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/**
 * 业务时区装配（OTD-VSO-DSN-CR-006 §10.7.3）。
 * <p>
 * 与 JSON 契约共用同一个时区来源 {@code iov.web.jackson.zone-id}：
 * 该键描述「{@code LocalDateTime} 数据属于哪个时区」，JSON 序列化与领域换算必须一致，
 * 否则同一时刻在不同链路上会相差整数小时。
 * <p>
 * 未配置时 fail-closed：禁止回退 JVM 默认时区（框架启动探针在 ISO 模式下同样会阻断）。
 *
 * @author VSO Team
 */
@Configuration
public class BusinessTimeZoneConfiguration {

    @Bean
    public BusinessTimeZone businessTimeZone(JacksonProperties jacksonProperties) {
        ZoneId zoneId = jacksonProperties.getZoneId();
        if (zoneId == null) {
            throw new IllegalStateException(
                    "iov.web.jackson.zone-id 未配置：LocalDateTime ↔ 瞬时换算需要显式业务时区（全 UTC 栈应为 UTC）");
        }
        return BusinessTimeZone.of(zoneId);
    }
}
