package net.hwyz.iov.cloud.otd.vso.api.contract.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 定金下单请求
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DownPaymentOrderRequest extends SelectedSaleModelRequest {

    /**
     * 下单人员类型
     */
    @NotNull(message = "下单人员类型不能为空")
    private Integer orderPersonType;
    /**
     * 购车方案
     */
    @NotNull(message = "购车方案不能为空")
    private Integer purchasePlan;
    /**
     * 订单人姓名
     */
    @NotBlank(message = "订单人姓名不能为空")
    private String orderPersonName;
    /**
     * 订单人证件类型
     */
    @NotNull(message = "订单人证件类型不能为空")
    private Integer orderPersonIdType;
    /**
     * 订单人证件号码
     */
    @NotBlank(message = "订单人证件号码不能为空")
    private String orderPersonIdNum;
    /**
     * 上牌城市
     */
    @NotBlank(message = "上牌城市不能为空")
    private String licenseCity;
    /**
     * 销售门店
     */
    @NotBlank(message = "销售门店不能为空")
    private String dealership;
    /**
     * 交付中心
     */
    @NotBlank(message = "交付中心不能为空")
    private String deliveryCenter;

}
