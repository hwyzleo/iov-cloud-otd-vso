package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单详情结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderDetailResult {

    /**
     * 订单 ID
     */
    private String orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 销售代码
     */
private String saleCode;

    private String saleModelCode;

    private Integer orderState;

    /**
     * 下单时间
     */
    private Date orderTime;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 购车人信息
     */
    private CustomerInfo customerInfo;

    /**
     * 车辆信息
     */
    private VehicleInfo vehicleInfo;

    /**
     * 组织机构信息
     */
    private OrganizationInfo organizationInfo;

    /**
     * 订单金额信息
     */
    private OrderAmount orderAmount;

    /**
     * 购车人类型
     */
    private Integer orderPersonType;

    /**
     * 购车计划
     */
    private Integer purchasePlan;

    /**
     * 购车人姓名
     */
    private String orderPersonName;

    /**
     * 购车人证件类型
     */
    private Integer orderPersonIdType;

    /**
     * 购车人证件号码
     */
    private String orderPersonIdNum;

    /**
     * 支付状态
     */
    private Integer payState;

    /**
     * 上牌城市编码
     */
    private String licenseCityCode;

    /**
     * 上牌城市名称
     */
    private String licenseCityName;

    /**
     * 下单门店编码
     */
    private String orderStoreCode;

    /**
     * 下单门店名称
     */
    private String orderStoreName;

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
     * 车型编码
     */
    private String modelCode;

    /**
     * 车型名称
     */
    private String modelName;

    /**
     * 配置编码
     */
    private String variantCode;

    /**
     * 配置名称
     */
    private String variantName;

    /**
     * 配置代码
     */
    private String configurationCode;

    /**
     * 选项编码列表
     */
    private List<String> optionCodes;

    /**
     * 选项明细列表
     */
    private List<VehicleInfo.OptionBreakdownItem> optionBreakdown;

    /**
     * 车型图片列表
     */
    private List<String> saleModelImages;

    /**
     * 车型描述
     */
    private String saleModelDesc;

    /**
     * 总价
     */
    private BigDecimal totalPrice;
}
