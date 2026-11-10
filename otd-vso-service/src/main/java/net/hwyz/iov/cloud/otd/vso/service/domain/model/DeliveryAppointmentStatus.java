package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 交付预约状态
 *
 * @author VSO Team
 */
@Getter
@AllArgsConstructor
public enum DeliveryAppointmentStatus {

    /** 已预约 */
    BOOKED("booked", "已预约"),

    /** 已交付完成 */
    COMPLETED("completed", "已完成"),

    /** 已取消 */
    CANCELLED("cancelled", "已取消");

    private final String value;
    private final String desc;

    public static DeliveryAppointmentStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
