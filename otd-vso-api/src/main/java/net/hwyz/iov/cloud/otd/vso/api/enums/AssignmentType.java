package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配车动作类型枚举
 */
@Getter
@AllArgsConstructor
public enum AssignmentType {

    ASSIGN("ASSIGN", "分配"),
    REASSIGN("REASSIGN", "换绑"),
    UNBIND("UNBIND", "解绑"),
    RELEASE("RELEASE", "释放"),
    EXPIRE("EXPIRE", "过期释放"),
    BIND("BIND", "绑定");

    private final String code;
    private final String name;

    public static AssignmentType fromCode(String code) {
        for (AssignmentType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}