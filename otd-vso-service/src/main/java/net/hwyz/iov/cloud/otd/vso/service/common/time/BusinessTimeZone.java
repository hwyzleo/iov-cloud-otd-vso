package net.hwyz.iov.cloud.otd.vso.service.common.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * 业务时区换算（OTD-VSO-DSN-CR-006 §10.7.3 清理项）。
 * <p>
 * VSO 的领域/PO 侧时间统一使用 {@link LocalDateTime}（墙上时间），JSON 侧输出为带时区的 ISO-8601。
 * 二者之间的换算必须使用**显式业务时区**（{@code iov.web.jackson.zone-id}），禁止使用
 * {@link ZoneId#systemDefault()} —— 容器基础镜像或部署环境时区一变，就会出现静默 8 小时漂移，
 * 且不会报错（历史缺陷：同一字段在不同接口相差 8 小时难以定位）。
 * <p>
 * 默认值与当前部署形态（全 UTC 栈：容器 `TZ=UTC` + DB 连接 UTC + `zone-id: UTC`）一致，
 * 仅用于单元测试等无 Spring 上下文的场景；生产由
 * {@code BusinessTimeZoneConfiguration} 按配置注入。
 *
 * @author VSO Team
 */
public final class BusinessTimeZone {

    private static final BusinessTimeZone UTC = new BusinessTimeZone(ZoneId.of("UTC"));

    private final ZoneId zoneId;

    private BusinessTimeZone(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * UTC 形态（与当前全 UTC 部署一致）。
     */
    public static BusinessTimeZone utc() {
        return UTC;
    }

    public static BusinessTimeZone of(ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId");
        return new BusinessTimeZone(zoneId);
    }

    public ZoneId zoneId() {
        return zoneId;
    }

    /**
     * 墙上时间 → 瞬时。
     */
    public Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(zoneId).toInstant();
    }

    /**
     * 瞬时 → 墙上时间。
     */
    public LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, zoneId);
    }

    /**
     * 墙上时间 → {@link Date}（仅用于仍需 {@code java.util.Date} 的遗留契约）。
     */
    public Date toDate(LocalDateTime localDateTime) {
        Instant instant = toInstant(localDateTime);
        return instant == null ? null : Date.from(instant);
    }

    /**
     * {@link Date} → 墙上时间。
     */
    public LocalDateTime fromDate(Date date) {
        return date == null ? null : toLocalDateTime(date.toInstant());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BusinessTimeZone that)) {
            return false;
        }
        return zoneId.equals(that.zoneId);
    }

    @Override
    public int hashCode() {
        return zoneId.hashCode();
    }

    @Override
    public String toString() {
        return "BusinessTimeZone(" + zoneId + ")";
    }
}
