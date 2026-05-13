package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理后台车辆销售订单
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleOrderMpt implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 订单业务ID（用于物理删除等操作）
     */
    private String orderId;

    /**
     * 订单号
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
     * 销售代码
     */
    private String saleCode;

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

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
     * 订单状态
     */
    private Integer orderState;

    /**
     * 订单状态时间
     */
    private Date orderStateTime;

    /**
     * 下单时间
     */
    private Date orderTime;

    /**
     * 下单人电话
     */
    private String orderPersonPhone;

    /**
     * 下单人员ID
     */
    private String orderPersonId;

    /**
     * 意向金支付时间
     */
    private Date earnestMoneyTime;

    /**
     * 意向金支付金额
     */
    private BigDecimal earnestMoneyAmount;

    /**
     * 定金支付时间
     */
    private Date downPaymentTime;

    /**
     * 定金支付金额
     */
    private BigDecimal downPaymentAmount;

    /**
     * 锁单时间
     */
    private Date lockTime;

    /**
     * 上牌城市
     */
    private String licenseCity;

    /**
     * 交付车辆
     */
    private String deliveryVin;

    /**
     * 交付中心
     */
    private String deliveryCenter;

    /**
     * 交付中心名称
     */
    private String deliveryCenterName;

    /**
     * 交付人员ID
     */
    private String deliveryPersonId;

    /**
     * 交付人员姓名
     */
    private String deliveryPersonName;

    /**
     * 创建时间
     */
    private Date createTime;

}
