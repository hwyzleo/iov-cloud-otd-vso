package net.hwyz.iov.cloud.otd.vso.api.vo.mpt;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.bean.BaseRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 管理后台车辆销售订单
 * <p>
 * 继承 BaseRequest 以复用框架 `params[beginTime]` / `params[endTime]` 查询参数契约。
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleSaleOrderMpt extends BaseRequest {

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
     * 车型编码（下单时车型快照）
     */
    private String modelCode;

    /**
     * 车型名称（下单时销售策略营销名称快照）
     */
    private String modelName;

    /**
     * 配置版本编码（下单时 Variant 快照）
     */
    private String variantCode;

    /**
     * 配置版本名称（下单时 Variant 销售策略营销名称快照）
     */
    private String variantName;

    /**
     * MDM 生产配置代码
     */
    private String configurationCode;

    /**
     * 选装代码列表
     */
    private List<String> optionCodes;

    /**
     * 选装明细快照
     */
    private List<OptionBreakdownMpt> optionBreakdown;

    /**
     * 客户类型
     */
    private String customerType;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 购车计划
     */
    private Integer purchasePlan;

    /**
     * 购车人类型
     */
    private Integer orderPersonType;

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
     * 订单金额快照
     */
    private OrderAmountMpt orderAmount;

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
     * <p>
     * 数据源为 {@code vso_order_party.mobile_encrypted}（加密字段），本期不采集、不输出明文；
     * 展示需要先确定解密服务与权限，属 O-009 另行设计范围，保持 null。
     */
    private String orderPersonPhone;

    /**
     * 下单人员ID
     * <p>
     * 数据源为 {@code vso_order_party.id_no_encrypted}（加密字段），本期不输出明文，保持 null。
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

    /**
     * 当前有效交付预约单号（未预约为空）
     */
    private String appointmentNo;

    /**
     * 交付预约时间（未预约为空）
     */
    private LocalDateTime appointmentTime;

    /**
     * 交付预约状态（未预约为空）
     */
    private String appointmentStatus;

}
