package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 小订单状态枚举
 *
 * @author VSO Team
 */
public enum SmallOrderStatus {

    /**
     * 待创建
     */
    PENDING_CREATE,

    /**
     * 待提交
     */
    PENDING_SUBMIT,

    /**
     * 待支付意向金
     */
    PENDING_EARNEST_PAYMENT,

    /**
     * 已支付意向金
     */
    EARNEST_PAID,

    /**
     * 待转正式订单
     */
    PENDING_TRANSFORM,

    /**
     * 已转正式订单
     */
    TRANSFORMED,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 已关闭
     */
    CLOSED,

    /**
     * 已失效
     */
    EXPIRED;

}
