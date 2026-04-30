package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 补贴状态枚举
 *
 * @author VSO Team
 */
public enum SubsidyStatus {

    /**
     * 待申请
     */
    PENDING_APPLY,

    /**
     * 申请中
     */
    APPLYING,

    /**
     * 待提交资料
     */
    PENDING_MATERIAL,

    /**
     * 审核中
     */
    REVIEWING,

    /**
     * 审核通过
     */
    APPROVED,

    /**
     * 审核驳回
     */
    REJECTED,

    /**
     * 补贴发放中
     */
    GRANTING,

    /**
     * 已发放
     */
    GRANTED,

    /**
     * 发放失败
     */
    GRANT_FAILED,

    /**
     * 已关闭
     */
    CLOSED;

}
