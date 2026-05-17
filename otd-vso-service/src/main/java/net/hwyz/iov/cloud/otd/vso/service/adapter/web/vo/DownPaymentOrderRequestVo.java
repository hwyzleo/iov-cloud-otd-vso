package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Map;

/**
 * 定金下单请求对象
 * <p>
 * 该对象融合了意向金下单和意向金转定金的参数：
 * <ul>
 *     <li>意向金下单参数：saleModelCode、orderNo、saleModelConfigType、licenseCityCode</li>
 *     <li>意向金转定金参数：customerType、paymentMethod、orderPersonType、orderPersonName、
 *         orderPersonIdType、orderPersonIdNum、purchasePlan、orderStoreCode、deliveryStoreCode</li>
 * </ul>
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownPaymentOrderRequestVo {

    /**
     * 销售车型代码，必填
     * <p>
     * 来自意向金下单参数
     */
    private String saleModelCode;

    /**
     * 订单号，可选
     * <p>
     * 来自意向金下单参数，用于从意向金订单转定金
     */
    private String orderNo;

    /**
     * 特征配置，必填
     * <p>
     * 来自意向金下单参数，Map形式，key为配置类型，value为配置值
     */
    private Map<String, String> saleModelConfigType;

    /**
     * 客户类型，可选
     * <p>
     * 来自意向金转定金参数
     * 可选值：
     * <ul>
     *     <li>personal - 个人客户（首期仅支持此类型）</li>
     * </ul>
     * 
     * @see net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType
     */
    private String customerType;

    /**
     * 支付方式，可选
     * <p>
     * 来自意向金转定金参数
     * 可选值：
     * <ul>
     *     <li>full_payment - 全款</li>
     *     <li>loan - 贷款</li>
     * </ul>
     * 
     * @see net.hwyz.iov.cloud.otd.vso.api.enums.PaymentMethod
     */
    private String paymentMethod;

    /**
     * 上牌城市代码，可选
     * <p>
     * 意向金下单和意向金转定金参数共有
     */
    private String licenseCityCode;

    /**
     * 订购人类型，可选
     * <p>
     * 来自意向金转定金参数
     */
    private Integer orderPersonType;

    /**
     * 购买计划，可选
     * <p>
     * 来自意向金转定金参数
     */
    private Integer purchasePlan;

    /**
     * 订购人姓名，可选
     * <p>
     * 来自意向金转定金参数
     */
    private String orderPersonName;

    /**
     * 订购人证件类型，可选
     * <p>
     * 来自意向金转定金参数
     */
    private Integer orderPersonIdType;

    /**
     * 订购人证件号码，可选
     * <p>
     * 来自意向金转定金参数
     */
    private String orderPersonIdNum;

    /**
     * 下单门店代码，可选
     * <p>
     * 来自意向金转定金参数
     */
    private String orderStoreCode;

    /**
     * 交付门店代码，可选
     * <p>
     * 来自意向金转定金参数
     */
    private String deliveryStoreCode;

}
