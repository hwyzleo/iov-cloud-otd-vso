package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

/**
 * 订单操作请求对象
 * <p>
 * 该对象被多个订单操作接口复用，不同接口所需字段不同：
 * <ul>
 *     <li>取消订单(cancel)：仅需 orderNo</li>
 *     <li>退款订单(requestRefund)：仅需 orderNo</li>
 *     <li>锁定订单(lock)：仅需 orderNo</li>
 *     <li>意向金转定金(earnestMoneyToDownPayment)：需 orderNo + 以下字段(可选)：
 *         <ul>
 *             <li>customerType - 客户类型</li>
 *             <li>paymentMethod - 支付方式</li>
 *             <li>orderPersonType - 订购人类型</li>
 *             <li>orderPersonName - 订购人姓名</li>
 *             <li>orderPersonIdType - 订购人证件类型</li>
 *             <li>orderPersonIdNum - 订购人证件号码</li>
 *             <li>purchasePlan - 购买计划</li>
 *             <li>licenseCityCode - 上牌城市代码</li>
 *             <li>orderStoreCode - 下单门店代码</li>
 *             <li>deliveryStoreCode - 交付门店代码</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 客户类型
     * <p>
     * 可选值：
     * <ul>
     *     <li>personal - 个人客户（首期仅支持此类型）</li>
     * </ul>
     * 
     * @see net.hwyz.iov.cloud.otd.vso.api.enums.CustomerType
     */
    private String customerType;

    /**
     * 支付方式
     * <p>
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
     * 订购人类型
     */
    private Integer orderPersonType;

    /**
     * 订购人姓名
     */
    private String orderPersonName;

    /**
     * 订购人证件类型
     */
    private Integer orderPersonIdType;

    /**
     * 订购人证件号码
     */
    private String orderPersonIdNum;

    /**
     * 购买计划
     */
    private Integer purchasePlan;

    /**
     * 上牌城市代码
     */
    private String licenseCityCode;

    /**
     * 下单门店代码
     */
    private String orderStoreCode;

    /**
     * 交付门店代码
     */
    private String deliveryStoreCode;

}
