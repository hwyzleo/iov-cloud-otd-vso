package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 合同状态枚举
 *
 * @author VSO Team
 */
public enum ContractStatus {

    /**
     * 草稿
     */
    DRAFT,

    /**
     * 已生成
     */
    GENERATED,

    /**
     * 签署中
     */
    SIGNING,

    /**
     * 已签署
     */
    SIGNED,

    /**
     * 已生效
     */
    EFFECTIVE,

    /**
     * 已失效
     */
    INVALID;

}
