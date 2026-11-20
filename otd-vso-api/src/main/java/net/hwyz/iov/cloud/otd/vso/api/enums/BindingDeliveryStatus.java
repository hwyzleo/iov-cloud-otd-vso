package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 绑车事实查询的交付核验状态（OTD-VSO-DSN-CR-017）
 * <p>
 * 只有 {@link #DELIVERED} 表示订单状态、订单—VIN 关联、交付记录与实际交付时间一致；
 * 任何缺失或冲突都不得降级为 {@link #NOT_DELIVERED}，而应返回
 * {@link BindingLookupStatus#INSUFFICIENT} 或 {@link BindingLookupStatus#INCONSISTENT}。
 *
 * @author VSO Team
 */
public enum BindingDeliveryStatus {

    /**
     * 已交付（650 与 900 使用同一口径）
     */
    DELIVERED,

    /**
     * 交付前状态且无冲突交付记录
     */
    NOT_DELIVERED
}
