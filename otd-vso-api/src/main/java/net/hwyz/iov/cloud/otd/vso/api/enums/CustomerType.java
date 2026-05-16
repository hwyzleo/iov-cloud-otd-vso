package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 客户类型枚举类
 * <p>
 * 首期仅支持个人客户，后续可扩展企业客户等类型
 *
 * @author VSO Team
 */
@AllArgsConstructor
public enum CustomerType {

    /** 个人客户 **/
    PERSONAL("personal", "个人客户");

    private final String code;
    private final String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static CustomerType valOf(String val) {
        return Arrays.stream(CustomerType.values())
                .filter(customerType -> customerType.code.equals(val))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValid(String val) {
        return valOf(val) != null;
    }

}