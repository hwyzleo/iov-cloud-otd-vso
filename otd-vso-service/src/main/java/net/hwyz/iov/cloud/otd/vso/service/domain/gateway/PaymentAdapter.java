package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

/**
 * 支付适配器
 *
 * @author VSO Team
 */
public interface PaymentAdapter {

    /**
     * 发起支付
     *
     * @param orderId 订单 ID
     * @param amount 金额
     * @param channel 支付渠道
     * @return 支付单号
     */
    String createPayment(String orderId, java.math.BigDecimal amount, String channel);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付单号
     * @return 支付状态
     */
    String queryPaymentStatus(String paymentNo);

    /**
     * 发起退款
     *
     * @param paymentNo 原支付单号
     * @param amount 退款金额
     * @param reason 退款原因
     * @return 退款单号
     */
    String refund(String paymentNo, java.math.BigDecimal amount, String reason);

    /**
     * 验证回调签名
     *
     * @param payload 回调报文
     * @param signature 签名
     * @return 是否有效
     */
    boolean verifyCallbackSignature(String payload, String signature);

}
