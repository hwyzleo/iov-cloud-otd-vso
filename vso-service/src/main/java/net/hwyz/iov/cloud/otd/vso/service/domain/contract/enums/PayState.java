package net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车辆订单支付状态枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum PayState {

    /** 意向金已支付 **/
    EARNEST_MONEY_PAID(10),
    /** 意向金已退款 **/
    EARNEST_MONEY_REFUNDED(15),
    /** 定金已支付 **/
    DOWN_PAYMENT_PAID(20),
    /** 定金已退款 **/
    DOWN_PAYMENT_REFUNDED(25),
    /** 尾款已支付 **/
    FINAL_PAYMENT_PAID(30),
    /** 尾款已退款 **/
    FINAL_PAYMENT_REFUNDED(35);

    public final Integer value;

    public static PayState valOf(Integer val) {
        return Arrays.stream(PayState.values())
                .filter(payState -> payState.value.equals(val))
                .findFirst()
                .orElse(null);
    }

}
