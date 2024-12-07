package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
import net.hwyz.iov.cloud.framework.common.web.domain.BaseRequest;

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
@EqualsAndHashCode(callSuper = true)
public class VehicleSaleOrderMpt extends BaseRequest {

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
     * 创建时间
     */
    private Date createTime;

}
