package net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车辆订单状态枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum OrderState {

    /** 心愿单 **/
    WISHLIST(100),
    /** 意向金待付款 **/
    EARNEST_MONEY_UNPAID(200),
    /** 意向金已付款 **/
    EARNEST_MONEY_PAID(210),
    /** 定金待付款 **/
    DOWN_PAYMENT_UNPAID(300),
    /** 定金已付款 **/
    DOWN_PAYMENT_PAID(310),
    /** 已取消 **/
    CANCEL(950);

    public final Integer value;

    public static OrderState valOf(Integer val) {
        return Arrays.stream(OrderState.values())
                .filter(orderState -> orderState.value.equals(val))
                .findFirst()
                .orElse(null);
    }

}
