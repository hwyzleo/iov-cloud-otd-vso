package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单支付响应
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentResponse {

    /**
     * 订单号
     */
    private String orderNum;
    /**
     * 支付商户
     */
    private String paymentMerchant;
    /**
     * 支付流水号
     */
    private String paymentReference;
    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;
    /**
     * 支付数据类型
     */
    private Integer paymentDateType;
    /**
     * 支付数据
     */
    private String paymentData;

}
