package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 订单类型枚举
 *
 * @author VSO Team
 */
public enum OrderType {

    /**
     * 小订单
     */
    SMALL,

    /**
     * 正式订单
     */
    FORMAL,

    /**
     * 手工订单
     */
    MANUAL,

    /**
     * 补单
     */
    REPAIR,

    /**
     * 变更单
     */
    CHANGE,

    /**
     * 退订申请
     */
    REFUND_APPLY,

    /**
     * 作废单
     */
    VOID,

    /**
     * 关闭单
     */
    CLOSED;

}
