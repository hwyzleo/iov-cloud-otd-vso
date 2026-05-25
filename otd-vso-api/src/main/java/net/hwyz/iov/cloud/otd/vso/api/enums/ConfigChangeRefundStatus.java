package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 改配退款状态枚举
 */
@Getter
@AllArgsConstructor
public enum ConfigChangeRefundStatus {

    PENDING("pending", "待处理"),
    PROCESSING("processing", "处理中"),
    COMPLETED("completed", "已完成"),
    FAILED("failed", "失败");

    private final String value;
    private final String desc;

    public static ConfigChangeRefundStatus fromValue(String value) {
        for (ConfigChangeRefundStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ConfigChangeRefundStatus: " + value);
    }
}
