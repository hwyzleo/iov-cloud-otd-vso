package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 订单主状态枚举
 *
 * @author VSO Team
 */
public enum MainStatus {

    /**
     * 待创建
     */
    PENDING_CREATE,

    /**
     * 待提交
     */
    PENDING_SUBMIT,

    /**
     * 待审核
     */
    PENDING_AUDIT,

    /**
     * 待锁单
     */
    PENDING_LOCK,

    /**
     * 已锁单
     */
    LOCKED,

    /**
     * 待配车
     */
    PENDING_VEHICLE_ASSIGN,

    /**
     * 已配车
     */
    VEHICLE_ASSIGNED,

    /**
     * 待签约
     */
    PENDING_CONTRACT,

    /**
     * 待付款
     */
    PENDING_PAYMENT,

    /**
     * 待交付
     */
    PENDING_DELIVERY,

    /**
     * 已交付
     */
    DELIVERED,

    /**
     * 已完成
     */
    COMPLETED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已关闭
     */
    CLOSED;

}
