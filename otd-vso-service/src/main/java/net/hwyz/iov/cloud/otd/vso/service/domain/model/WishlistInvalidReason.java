package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 心愿单失效原因枚举
 *
 * @author VSO Team
 */
@Getter
@AllArgsConstructor
public enum WishlistInvalidReason {

    SALE_MODEL_OFF_SHELF("SALE_MODEL_OFF_SHELF", "销售车型已下架"),
    MODEL_OFF_SHELF("MODEL_OFF_SHELF", "Model 销售策略已失效"),
    VARIANT_OFF_SHELF("VARIANT_OFF_SHELF", "Variant 销售策略已失效"),
    CONFIGURATION_OFF_SHELF("CONFIGURATION_OFF_SHELF", "Configuration 不在销售白名单"),
    OPTION_OFF_SHELF("OPTION_OFF_SHELF", "OptionCode 不可售"),
    REGION_RESTRICTED("REGION_RESTRICTED", "区域限制");

    private final String code;
    private final String message;

    public static WishlistInvalidReason fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (WishlistInvalidReason reason : values()) {
            if (reason.code.equals(code)) {
                return reason;
            }
        }
        return null;
    }
}
