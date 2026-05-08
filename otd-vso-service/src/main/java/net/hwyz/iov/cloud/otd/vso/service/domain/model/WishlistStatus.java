package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 心愿单状态枚举类
 *
 * @author VSO Team
 */
@AllArgsConstructor
public enum WishlistStatus {

    ACTIVE(1),
    DELETED(0);

    public final Integer value;

    public Integer getValue() {
        return value;
    }

    public static WishlistStatus valOf(Integer val) {
        return Arrays.stream(WishlistStatus.values())
                .filter(status -> status.value.equals(val))
                .findFirst()
                .orElse(null);
    }

    public static WishlistStatus fromValue(Integer val) {
        return valOf(val);
    }

}