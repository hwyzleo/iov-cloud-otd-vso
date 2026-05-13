package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * 订单列表结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderListResult {

    /**
     * 订单 ID
     */
    private String orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 小订单号
     */
    private String smallOrderNo;

    /**
     * 订单类型编码
     */
    private String orderType;

    /**
     * 订单类型名称
     */
    private String orderTypeName;

    /**
     * 订单来源编码
     */
    private String orderSource;

    /**
     * 订单来源名称
     */
    private String orderSourceName;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 品牌编码
     */
    private String brandCode;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 销售车型编码
     */
    private String saleModel;

    /**
     * 销售车型名称
     */
    private String saleModelName;

    /**
     * 归属区域编码
     */
    private String regionCode;

    /**
     * 归属区域名称
     */
    private String regionName;

    /**
     * 生产配置编码
     */
    private String buildConfigCode;

    /**
     * 展示名称
     */
    private String displayName;
}
