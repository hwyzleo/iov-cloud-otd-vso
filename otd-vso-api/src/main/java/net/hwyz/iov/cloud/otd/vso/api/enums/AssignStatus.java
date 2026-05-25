package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配车状态枚举
 */
@Getter
@AllArgsConstructor
public enum AssignStatus {

    ASSIGNED("ASSIGNED", "已分配"),
    BOUND("BOUND", "已绑定"),
    RELEASED("RELEASED", "已释放"),
    EXPIRED("EXPIRED", "已过期"),
    UNBOUND("UNBOUND", "已解绑");

    private final String code;
    private final String name;

    public static AssignStatus fromCode(String code) {
        for (AssignStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}