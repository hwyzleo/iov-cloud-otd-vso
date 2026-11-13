package net.hwyz.iov.cloud.otd.vso.service.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentGatewayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.RefundGatewayResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Mock 支付网关适配器
 * <p>
 * 契约（设计 §13）：
 * createPayment → externalTradeNo=MOCK-{paymentNo}、paymentMerchant=MOCK_PAY、
 * paymentReference=mock://cashier/{paymentNo}、gatewayStatus=PROCESSING；
 * 活动支付查询固定返回 PROCESSING（不得返回 PAID）；
 * refund → externalRefundNo=MOCK-{refundItemId}、gatewayStatus=processing。
 * 终态只能由签名回调驱动。
 *
 * @author VSO Team
 */
@Slf4j
@Component
public class MockPaymentAdapter implements PaymentAdapter {

    @Override
    public boolean isMock() {
        return true;
    }

    @Override
    public PaymentGatewayResult createPayment(String paymentNo, BigDecimal amount, String channel) {
        log.info("[MOCK] 发起支付: paymentNo={}, amount={}, channel={}", paymentNo, amount, channel);
        return PaymentGatewayResult.builder()
                .externalTradeNo("MOCK-" + paymentNo)
                .paymentMerchant("MOCK_PAY")
                .paymentReference("mock://cashier/" + paymentNo)
                .gatewayStatus("PROCESSING")
                .build();
    }

    @Override
    public String queryPaymentStatus(String paymentNo) {
        log.info("[MOCK] 查询支付状态: paymentNo={} → PROCESSING（终态由回调驱动）", paymentNo);
        return "PROCESSING";
    }

    @Override
    public RefundGatewayResult refundItem(String refundItemId, String paymentNo, BigDecimal amount, String reason) {
        log.info("[MOCK] 发起退款: refundItemId={}, paymentNo={}, amount={}, reason={}",
                refundItemId, paymentNo, amount, reason);
        return RefundGatewayResult.builder()
                .externalRefundNo("MOCK-" + refundItemId)
                .gatewayStatus("processing")
                .build();
    }

    @Override
    public String refund(String paymentNo, BigDecimal amount, String reason) {
        log.info("[MOCK] 发起退款(兼容): paymentNo={}, amount={}, reason={}", paymentNo, amount, reason);
        return "MOCK-REF-" + paymentNo;
    }

    @Override
    public boolean verifyCallbackSignature(String payload, String signature) {
        // 真实签名校验由 CallbackSignatureService 执行；此处仅占位
        return true;
    }
}
