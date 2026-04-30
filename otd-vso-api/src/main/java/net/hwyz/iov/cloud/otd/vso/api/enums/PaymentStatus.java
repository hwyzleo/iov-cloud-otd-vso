package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 支付状态枚举
 *
 * @author VSO Team
 */
public enum PaymentStatus {

    /**
     * 待支付
     */
    PENDING_PAYMENT,

    /**
     * 部分支付
     */
    PARTIALLY_PAID,

    /**
     * 已支付
     */
    PAID,

    /**
     * 支付处理中
     */
    PROCESSING,

    /**
     * 支付失败
     */
    PAYMENT_FAILED,

    /**
     * 已退款
     */
    REFUNDED,

    /**
     * 部分退款
     */
    PARTIALLY_REFUNDED,

    /**
     * 退款处理中
     */
    REFUNDING,

    /**
     * 退款失败
     */
    REFUND_FAILED,

    /**
     * 已关闭
     */
    CLOSED;

}
