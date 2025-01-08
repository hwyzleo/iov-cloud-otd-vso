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
    /** 已锁单，安排生产 **/
    ARRANGE_PRODUCTION(400),
    /** 已分配车辆 **/
    ALLOCATION_VEHICLE(450),
    /** 发运申请 **/
    APPLY_TRANSPORT(470),
    /** 待运输 **/
    PREPARE_TRANSPORT(500),
    /** 已发车，运输中 **/
    TRANSPORTING(550),
    /** 验收入库，待交付 **/
    PREPARE_DELIVER(600),
    /** 尾款已付款 **/
    FINAL_PAYMENT_PAID(620),
    /** 已开票 **/
    INVOICED(630),
    /** 已交付 **/
    DELIVERED(650),
    /** 已激活 **/
    ACTIVATED(700),
    /** 退车申请 **/
    RETURN_APPLY(800),
    /** 退车入库 **/
    RETURN_STORAGE(820),
    /** 退车审核 **/
    RETURN_AUDIT(840),
    /** 退车完成 **/
    RETURN_COMPLETED(860),
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
