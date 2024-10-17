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
    /** 意向金（小定）待付款 **/
    EARNEST_MONEY_UNPAID(200),
    /** 意向金（小定）已付款 **/
    EARNEST_MONEY_PAID(210);

    public final Integer value;

    public static OrderState valOf(Integer val) {
        return Arrays.stream(OrderState.values())
                .filter(orderState -> orderState.value.equals(val))
                .findFirst()
                .orElse(null);
    }

}
