package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 审批状态枚举
 *
 * @author VSO Team
 */
public enum ApprovalStatus {

    /**
     * 待创建
     */
    PENDING_CREATE,

    /**
     * 待提交
     */
    PENDING_SUBMIT,

    /**
     * 审批中
     */
    APPROVING,

    /**
     * 审批通过
     */
    APPROVED,

    /**
     * 审批驳回
     */
    REJECTED,

    /**
     * 已撤回
     */
    WITHDRAWN,

    /**
     * 已取消
     */
    CANCELLED,

    /**
     * 执行中
     */
    EXECUTING,

    /**
     * 执行完成
     */
    EXECUTED,

    /**
     * 执行失败
     */
    EXECUTION_FAILED,

    /**
     * 已关闭
     */
    CLOSED;

}
