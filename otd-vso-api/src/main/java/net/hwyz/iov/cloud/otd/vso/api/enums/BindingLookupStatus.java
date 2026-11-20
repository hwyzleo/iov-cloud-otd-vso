package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 绑车事实查询结果分类（OTD-VSO-DSN-CR-017）
 * <p>
 * 只表达 VSO 本地事实的读取结论，不是绑车资格结论：
 * <ul>
 *   <li>{@link #MATCHED}：唯一可解释的有效账号—订单—VIN 关联；</li>
 *   <li>{@link #NO_MATCH}：没有同时匹配账号与 VIN 的有效订单，不泄露他人订单；</li>
 *   <li>{@link #AMBIGUOUS}：存在两个以上不同订单候选，不自动选择最新或已交付单；</li>
 *   <li>{@link #INSUFFICIENT}：事实缺失（历史态、交付证据不完整），不得补造；</li>
 *   <li>{@link #INCONSISTENT}：现存有效事实互相矛盾，不挑任意一条。</li>
 * </ul>
 *
 * @author VSO Team
 */
public enum BindingLookupStatus {

    /**
     * 唯一可解释关联
     */
    MATCHED,

    /**
     * 无同时匹配账号与 VIN 的有效订单
     */
    NO_MATCH,

    /**
     * 多个订单候选
     */
    AMBIGUOUS,

    /**
     * 事实不足
     */
    INSUFFICIENT,

    /**
     * 事实冲突
     */
    INCONSISTENT
}
