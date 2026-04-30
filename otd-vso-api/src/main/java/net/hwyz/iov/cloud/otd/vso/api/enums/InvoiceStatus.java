package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 发票状态枚举
 *
 * @author VSO Team
 */
public enum InvoiceStatus {

    /**
     * 待开票
     */
    PENDING_ISSUE,

    /**
     * 开票中
     */
    ISSUING,

    /**
     * 已开票
     */
    ISSUED,

    /**
     * 开票失败
     */
    ISSUE_FAILED,

    /**
     * 已作废
     */
    VOID,

    /**
     * 已关闭
     */
    CLOSED;

}
