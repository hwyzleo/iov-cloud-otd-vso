package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import java.math.BigDecimal;

/**
 * 支付适配器
 * <p>
 * 外部支付/退款网关统一入口；Mock 实现不得固定返回支付成功。
 *
 * @author VSO Team
 */
public interface PaymentAdapter {

    /**
     * 是否为 mock（模拟）支付渠道。
     * <p>
     * 用于「mock 环境由服务端扮演渠道自回调」的默认判定（OTD-VSO-DSN-CR-007）：
     * 装配的是 mock 渠道即默认开启自回调，真实渠道装配后自动关闭，不依赖外部配置是否生效。
     */
    default boolean isMock() {
        return false;
    }

    /**
     * 发起支付
     *
     * @param paymentNo 支付单号（本地已生成，作为网关幂等键）
     * @param amount    金额
     * @param channel   支付渠道
     * @return 网关受理结果
     */
    PaymentGatewayResult createPayment(String paymentNo, BigDecimal amount, String channel);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付单号
     * @return 网关状态
     */
    String queryPaymentStatus(String paymentNo);

    /**
     * 发起退款
     *
     * @param refundItemId 退款明细业务 ID（作为网关幂等键）
     * @param paymentNo    原支付单号
     * @param amount       退款金额
     * @param reason       退款原因
     * @return 网关受理结果
     */
    RefundGatewayResult refundItem(String refundItemId, String paymentNo, BigDecimal amount, String reason);

    /**
     * 兼容旧调用的退款接口
     *
     * @param paymentNo 原支付单号
     * @param amount    退款金额
     * @param reason    退款原因
     * @return 外部退款单号
     */
    String refund(String paymentNo, BigDecimal amount, String reason);

    /**
     * 验证回调签名
     *
     * @param payload   回调报文
     * @param signature 签名
     * @return 是否有效
     */
    boolean verifyCallbackSignature(String payload, String signature);
}
