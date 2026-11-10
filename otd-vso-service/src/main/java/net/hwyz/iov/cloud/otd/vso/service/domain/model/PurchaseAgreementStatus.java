package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 购车协议模板状态
 *
 * @author VSO Team
 */
@Getter
@AllArgsConstructor
public enum PurchaseAgreementStatus {

    /** 草稿 */
    DRAFT("draft", "草稿"),

    /** 生效 */
    ACTIVE("active", "生效"),

    /** 下架 */
    OFF_SHELF("off_shelf", "下架");

    private final String value;
    private final String desc;

    public static PurchaseAgreementStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(status -> status.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
