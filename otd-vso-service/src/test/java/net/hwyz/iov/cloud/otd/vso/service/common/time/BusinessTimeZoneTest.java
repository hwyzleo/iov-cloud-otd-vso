package net.hwyz.iov.cloud.otd.vso.service.common.time;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 业务时区换算测试（OTD-VSO-DSN-CR-006 §10.7.3）。
 * <p>
 * 守护目标：LocalDateTime ↔ 瞬时换算**只**由显式业务时区决定，
 * 与 JVM 默认时区（容器 TZ）无关 —— 历史缺陷正是 systemDefault 导致的静默 8 小时漂移。
 *
 * @author VSO Team
 */
class BusinessTimeZoneTest {

    private static final LocalDateTime WALL = LocalDateTime.parse("2026-10-14T13:00:00");

    private TimeZone originalJvmZone;

    @AfterEach
    void restoreJvmZone() {
        if (originalJvmZone != null) {
            TimeZone.setDefault(originalJvmZone);
            originalJvmZone = null;
        }
    }

    @Test
    @DisplayName("UTC 业务时区：墙上时间与瞬时同值")
    void utc_墙上时间与瞬时同值() {
        BusinessTimeZone utc = BusinessTimeZone.utc();
        assertEquals(Instant.parse("2026-10-14T13:00:00Z"), utc.toInstant(WALL));
        assertEquals(WALL, utc.toLocalDateTime(Instant.parse("2026-10-14T13:00:00Z")));
        assertEquals(Date.from(Instant.parse("2026-10-14T13:00:00Z")), utc.toDate(WALL));
        assertEquals(WALL, utc.fromDate(Date.from(Instant.parse("2026-10-14T13:00:00Z"))));
    }

    @Test
    @DisplayName("Asia/Shanghai 业务时区：墙上时间比 UTC 早 8 小时")
    void shanghai_墙上时间减八小时() {
        BusinessTimeZone shanghai = BusinessTimeZone.of(ZoneId.of("Asia/Shanghai"));
        assertEquals(Instant.parse("2026-10-14T05:00:00Z"), shanghai.toInstant(WALL));
        assertEquals(WALL, shanghai.toLocalDateTime(Instant.parse("2026-10-14T05:00:00Z")));
    }

    @Test
    @DisplayName("与 JVM 默认时区无关：切换 TZ 后换算结果不变")
    void 与JVM默认时区无关() {
        originalJvmZone = TimeZone.getDefault();
        BusinessTimeZone utc = BusinessTimeZone.utc();

        for (String jvmZone : new String[]{"UTC", "Asia/Shanghai", "America/New_York"}) {
            TimeZone.setDefault(TimeZone.getTimeZone(jvmZone));
            assertEquals(Instant.parse("2026-10-14T13:00:00Z"), utc.toInstant(WALL),
                    "JVM 时区=" + jvmZone + " 时 UTC 换算不应变化");
            assertEquals(WALL, utc.fromDate(Date.from(Instant.parse("2026-10-14T13:00:00Z"))),
                    "JVM 时区=" + jvmZone + " 时往返不应变化");
        }
    }

    @Test
    @DisplayName("null 透传：空值不产生 1970 时间")
    void null透传() {
        BusinessTimeZone utc = BusinessTimeZone.utc();
        assertNull(utc.toInstant(null));
        assertNull(utc.toLocalDateTime(null));
        assertNull(utc.toDate(null));
        assertNull(utc.fromDate(null));
    }

    @Test
    @DisplayName("zoneId 对外可见，便于日志与排查")
    void zoneId可见() {
        assertEquals(ZoneId.of("UTC"), BusinessTimeZone.utc().zoneId());
        assertEquals(ZoneId.of("Asia/Shanghai"),
                BusinessTimeZone.of(ZoneId.of("Asia/Shanghai")).zoneId());
    }
}
