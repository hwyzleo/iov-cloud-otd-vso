package net.hwyz.iov.cloud.otd.vso.service.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 支付适配器存根实现
 * TODO: 对接实际支付网关后替换此实现
 */
@Slf4j
@Component
public class StubPaymentAdapter implements PaymentAdapter {

    @Override
    public String createPayment(String orderId, BigDecimal amount, String channel) {
        String paymentNo = "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.warn("[STUB] 发起支付: orderId={}, amount={}, channel={}, paymentNo={}", orderId, amount, channel, paymentNo);
        return paymentNo;
    }

    @Override
    public String queryPaymentStatus(String paymentNo) {
        log.warn("[STUB] 查询支付状态: paymentNo={}", paymentNo);
        return "PAID";
    }

    @Override
    public String refund(String paymentNo, BigDecimal amount, String reason) {
        String refundNo = "REF" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.warn("[STUB] 发起退款: paymentNo={}, amount={}, reason={}, refundNo={}", paymentNo, amount, reason, refundNo);
        return refundNo;
    }

    @Override
    public boolean verifyCallbackSignature(String payload, String signature) {
        log.warn("[STUB] 验证回调签名: signature={}", signature);
        return true;
    }
}
