package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 付款方式枚举类
 *
 * @author VSO Team
 */
@AllArgsConstructor
public enum PaymentMethod {

    /** 全款 **/
    FULL_PAYMENT("full_payment", "全款"),
    /** 贷款 **/
    LOAN("loan", "贷款");

    private final String code;
    private final String desc;

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static PaymentMethod valOf(String val) {
        return Arrays.stream(PaymentMethod.values())
                .filter(paymentMethod -> paymentMethod.code.equals(val))
                .findFirst()
                .orElse(null);
    }

    public static boolean isValid(String val) {
        return valOf(val) != null;
    }

}