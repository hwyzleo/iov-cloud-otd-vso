package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.OrderAmount;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.CustomerInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.OrganizationInfo;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.VehicleInfo;

import java.util.Date;

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
     * 订单号
     */
    private String orderNum;

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 下单时间
     */
    private Date orderTime;

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
     * 上牌城市编码
     */
    private String licenseCityCode;

    /**
     * 门店编码
     */
    private String dealershipCode;

    /**
     * 交付中心编码
     */
    private String deliveryCenterCode;

    /**
     * 车型配置类型
     */
    private String saleModelConfigType;

    /**
     * 车型配置名称
     */
    private String saleModelConfigName;

    /**
     * 车型配置价格
     */
    private java.math.BigDecimal saleModelConfigPrice;

    /**
     * 车型图片列表
     */
    private java.util.List<String> saleModelImages;

    /**
     * 车型描述
     */
    private String saleModelDesc;

    /**
     * 总价
     */
    private java.math.BigDecimal totalPrice;
}
