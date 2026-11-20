package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 绑车事实查询的稳定业务原因码（OTD-VSO-DSN-CR-017）
 * <p>
 * 不是新增数字错误码：正常业务核验结果通过成功响应承载，原因码只指示核查方向。
 * {@link BindingLookupStatus#MATCHED} 时原因码为空。
 *
 * @author VSO Team
 */
public enum BindingReasonCode {

    /**
     * 没有同时匹配账号与 VIN 的有效订单（不区分不存在的 VIN 与只属于他人的 VIN）
     */
    NO_ACCOUNT_VIN_MATCH,

    /**
     * 存在两个以上不同订单候选
     */
    MULTIPLE_ORDER_MATCHES,

    /**
     * 交付记录、实际交付时间或必要关联证据缺失
     */
    MISSING_DELIVERY_EVIDENCE,

    /**
     * 订单—VIN 关联与交付记录 VIN 不一致
     */
    VIN_EVIDENCE_MISMATCH,

    /**
     * 订单状态与交付记录互相矛盾，或存在取消/关闭/失效等无法安全解释的状态
     */
    STATE_EVIDENCE_CONFLICT,

    /**
     * 历史 700 或未知状态枚举，未迁移事实不得按已交付推定
     */
    UNSUPPORTED_LEGACY_STATE,

    /**
     * 保留码：账号到订单平台用户标识的权威映射无法确定时使用。
     * 本期仓库核查确认 accountId 与 vso_order_party.user_id 同命名空间，
     * 缺失/非法账号按参数错误处理，因此当前实现不返回该码。
     */
    IDENTITY_MAPPING_UNRESOLVED
}
