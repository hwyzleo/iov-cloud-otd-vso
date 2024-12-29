package net.hwyz.iov.cloud.otd.vso.service.domain.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车辆订单操作枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum OrderOperate {

    /** 订单创建 **/
    ORDER_CREATE,
    /** 订单支付 **/
    ORDER_PAY,
    /** 意向金转定金 **/
    EARNEST_MONEY_TO_DOWN_PAYMENT,
    /** 申请退款 **/
    APPLY_REFUND,
    /** 订单锁定 **/
    ORDER_LOCK,
    /** 分配交付人员 **/
    ASSIGN_DELIVERY_PERSON,
    /** 分配车辆 **/
    ASSIGN_VEHICLE,
    /** 准备运输 **/
    PREPARE_TRANSPORT,
    /** 运输中 **/
    TRANSPORTING,
    /** 准备交付 **/
    PREPARE_DELIVERY,
    /** 已交付 **/
    DELIVERED,
    /** 车辆激活 **/
    VEHICLE_ACTIVATE,
    /** 订单取消 **/
    ORDER_CANCEL;

}
