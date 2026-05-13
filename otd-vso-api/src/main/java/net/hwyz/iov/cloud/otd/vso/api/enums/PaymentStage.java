package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 支付阶段枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum PaymentStage {

    EARNEST_MONEY,
    DOWN_PAYMENT,
    TAIL_PAYMENT,
    FINANCE_DISBURSEMENT;

    public static PaymentStage valOf(String val) {
        return Arrays.stream(PaymentStage.values())
                .filter(paymentStage -> paymentStage.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}