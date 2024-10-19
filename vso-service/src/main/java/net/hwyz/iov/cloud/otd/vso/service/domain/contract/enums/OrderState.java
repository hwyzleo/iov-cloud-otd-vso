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
    /** 安排生产 **/
    ARRANGE_PRODUCTION(400),
    /** 分配车辆 **/
    ALLOCATION_VEHICLE(450),
    /** 待运输 **/
    PREPARE_TRANSPORT(500),
    /** 运输中 **/
    TRANSPORTING(550),
    /** 待交付 **/
    PREPARE_DELIVER(600),
    /** 已交付 **/
    DELIVERED(650),
    /** 已激活 **/
    ACTIVATED(700),
    /** 退款申请 **/
    REFUND_APPLY(920),
    /** 退款完成 **/
    REFUND_COMPLETE(925),
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
