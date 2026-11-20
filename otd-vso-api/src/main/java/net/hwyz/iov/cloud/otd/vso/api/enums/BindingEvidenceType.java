package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 绑车事实查询的证据引用类型（OTD-VSO-DSN-CR-017）
 * <p>
 * 引用只包含业务标识与真实持久化版本，不暴露自增主键，不用查询时间冒充事实版本。
 *
 * @author VSO Team
 */
public enum BindingEvidenceType {

    /**
     * 订单事实
     */
    ORDER,

    /**
     * 本账号匹配的订单主体行
     */
    PARTY,

    /**
     * 交付记录
     */
    DELIVERY
}
