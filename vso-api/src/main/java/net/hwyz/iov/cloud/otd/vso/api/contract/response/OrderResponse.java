package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.contract.SelectedSaleModel;

import java.util.Date;

/**
 * 订单详情响应
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderResponse extends SelectedSaleModel {
    /**
     * 订单编号
     */
    private String orderNum;
    /**
     * 订单状态
     */
    private Integer orderState;
    /**
     * 下单时间
     */
    private Date orderTime;
    /**
     * 订单人类型
     */
    private Integer orderPersonType;
    /**
     * 购车方案
     */
    private Integer purchasePlan;
    /**
     * 订单人名称
     */
    private String orderPersonName;
    /**
     * 订单人证件类型
     */
    private Integer orderPersonIdType;
    /**
     * 订单人证件号
     */
    private String orderPersonIdNum;
    /**
     * 上牌城市名称
     */
    private String licenseCityName;
    /**
     * 上牌城市代码
     */
    private String licenseCityCode;
    /**
     * 销售门店名称
     */
    private String dealershipName;
    /**
     * 销售门店代码
     */
    private String dealershipCode;
    /**
     * 交付中心名称
     */
    private String deliveryCenterName;
    /**
     * 交付中心代码
     */
    private String deliveryCenterCode;
}
