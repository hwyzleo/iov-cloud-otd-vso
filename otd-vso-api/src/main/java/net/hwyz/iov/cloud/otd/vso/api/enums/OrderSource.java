package net.hwyz.iov.cloud.otd.vso.api.enums;

/**
 * 订单来源枚举
 *
 * @author VSO Team
 */
public enum OrderSource {

    /**
     * C 端自主下单
     */
    CAPP,

    /**
     * 销售代客下单
     */
    SALES,

    /**
     * 门店代客下单
     */
    STORE,

    /**
     * 运营补录
     */
    OPERATION,

    /**
     * 外部导入
     */
    IMPORT,

    /**
     * 活动订单
     */
    ACTIVITY,

    /**
     * 小订单转正式
     */
    SMALL_TO_FORMAL;

}
