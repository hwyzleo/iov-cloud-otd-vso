package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 补款场景枚举
 */
@Getter
@AllArgsConstructor
public enum SupplementaryPaymentScene {

    CONFIG_CHANGE("config_change", "改配补款"),
    EARNEST_TO_DOWN("earnest_to_down", "意向金转定金差额");

    private final String value;
    private final String desc;

    public static SupplementaryPaymentScene fromValue(String value) {
        for (SupplementaryPaymentScene scene : values()) {
            if (scene.value.equals(value)) {
                return scene;
            }
        }
        throw new IllegalArgumentException("Unknown SupplementaryPaymentScene: " + value);
    }
}
