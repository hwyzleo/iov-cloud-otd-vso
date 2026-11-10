package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 退款状态（主单与明细共用）
 *
 * @author VSO Team
 */
@Getter
@AllArgsConstructor
public enum RefundStatus {

    /** 待处理 */
    PENDING("pending", "待处理"),

    /** 处理中 */
    PROCESSING("processing", "处理中"),

    /** 已完成 */
    COMPLETED("completed", "已完成"),

    /** 失败 */
    FAILED("failed", "失败");

    private final String value;
    private final String desc;

    public static RefundStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 是否为终态
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }
}
