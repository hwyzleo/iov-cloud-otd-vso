package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * 下单门店编码
     */
    private String orderStoreCode;

    /**
     * 归属门店编码
     */
    private String ownerStoreCode;

    /**
     * 归属门店名称
     */
    private String ownerStoreName;

    /**
     * 归属区域编码
     */
    private String ownerRegionCode;

    /**
     * 归属区域名称
     */
    private String ownerRegionName;

    /**
     * 交付门店编码
     */
    private String deliveryStoreCode;

    /**
     * 交付门店名称
     */
    private String deliveryStoreName;

    /**
     * 交付区域编码
     */
    private String deliveryRegionCode;

    /**
     * 交付区域名称
     */
    private String deliveryRegionName;

    /**
     * 生产配置编码
     */
    private String buildConfigCode;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 销售车型配置类型
     * key: 销售车型配置类型
     * value: 销售车型配置代码
     */
    private Map<String, String> saleModelConfigType;

    /**
     * 销售车型配置名称
     * key: 销售车型配置类型
     * value: 销售车型配置名称
     */
    private Map<String, String> saleModelConfigName;

    /**
     * 销售车型图片集
     */
    private List<String> saleModelImages;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 销售车型描述
     */
    private String saleModelDesc;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 上牌城市
     */
    private String licenseCity;

}
