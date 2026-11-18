package net.hwyz.iov.cloud.otd.vso.service.common.util;

import net.hwyz.iov.cloud.framework.common.util.ExplicitTimeUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * 出站事件时间格式化工具（OTD-VSO-DSN-CR-012）
 * <p>
 * 事件信封与载荷时间统一为 ISO-8601：必带时区偏移、固定 3 位毫秒。
 * 时刻来源遵循框架时间治理（FW-COMMON-DSN-CR-001）：事件时刻取 {@link ExplicitTimeUtil#nowInstant()}，
 * 时区由调用方显式传入（生产来自 {@code iov.web.jackson.zone-id}），禁止读取 JVM 默认时区。
 *
 * @author VSO Team
 */
public final class VsoEventTime {

    /**
     * 事件时间格式：ISO-8601 带偏移，固定 3 位毫秒（例 2026-11-18T13:05:27.412+08:00）
     */
    private static final DateTimeFormatter EVENT_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

    private VsoEventTime() {
    }

    /**
     * 当前事件时刻
     *
     * @return 绝对时刻
     */
    public static Instant now() {
        return ExplicitTimeUtil.nowInstant();
    }

    /**
     * 格式化绝对时刻
     *
     * @param instant 绝对时刻，非 null
     * @param zone    业务时区，非 null
     * @return ISO-8601 文本
     */
    public static String format(Instant instant, ZoneId zone) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(zone, "zone");
        return EVENT_FORMATTER.format(ExplicitTimeUtil.toOffsetDateTime(instant, zone));
    }

    /**
     * 格式化时间对象（null 透传）
     *
     * @param date 时间，可为 null
     * @param zone 业务时区，非 null
     * @return ISO-8601 文本；入参为 null 时返回 null
     */
    public static String format(Date date, ZoneId zone) {
        if (date == null) {
            return null;
        }
        return format(date.toInstant(), zone);
    }
}
