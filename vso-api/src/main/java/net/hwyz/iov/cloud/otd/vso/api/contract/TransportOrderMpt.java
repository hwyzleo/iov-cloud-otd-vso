package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 管理后台运输相关车辆销售订单
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransportOrderMpt extends BaseRequest {

    /**
     * 主键
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

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
     * 锁单时间
     */
    private Date lockTime;

    /**
     * 发运申请时间
     */
    private Date transportApplyTime;

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
     * 运输申请人员ID
     */
    private String transportApplyPersonId;

    /**
     * 运输申请人员姓名
     */
    private String transportApplyPersonName;

    /**
     * 交付人员ID
     */
    private String deliveryPersonId;

    /**
     * 交付人员姓名
     */
    private String deliveryPersonName;

}
