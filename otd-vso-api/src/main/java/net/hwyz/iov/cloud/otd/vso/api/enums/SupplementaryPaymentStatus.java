package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 改配补款状态枚举
 */
@Getter
@AllArgsConstructor
public enum SupplementaryPaymentStatus {

    PENDING("pending", "待支付"),
    COMPLETED("completed", "已完成"),
    CANCELLED("cancelled", "已取消"),
    EXPIRED("expired", "已过期");

    private final String value;
    private final String desc;

    public static SupplementaryPaymentStatus fromValue(String value) {
        for (SupplementaryPaymentStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown SupplementaryPaymentStatus: " + value);
    }
}
