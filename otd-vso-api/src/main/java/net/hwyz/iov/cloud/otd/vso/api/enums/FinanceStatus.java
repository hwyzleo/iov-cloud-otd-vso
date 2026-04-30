package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 金融状态枚举
 *
 * @author VSO Team
 */
public enum FinanceStatus {

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
     * 资料审核中
     */
    MATERIAL_REVIEWING,

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
     * 放款中
     */
    DISBURSING,

    /**
     * 已放款
     */
    DISBURSED,

    /**
     * 放款失败
     */
    DISBURSEMENT_FAILED,

    /**
     * 已关闭
     */
    CLOSED;

}
