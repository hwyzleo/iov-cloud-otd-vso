package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PaymentCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.PaymentAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Payment;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock 支付渠道自回调（OTD-VSO-DSN-CR-007）。
 * <p>
 * 背景：早期由**手机端**持密钥伪造网关回调（iOS `MockPaymentGateway`），存在两个问题：
 * <ol>
 *   <li>客户端持有回调签名密钥，任何人拿到包就能给任意订单伪造 PAID（信任边界被打破）；</li>
 *   <li>为联调必须在生产网关开放 {@code /api/open/vsoCallback/**} 公网路由（契约污染）。</li>
 * </ol>
 * 现改为**服务端自己扮演支付渠道**：mock 模式下支付单创建后由本服务构造回调报文、
 * 用 {@link CallbackSignatureService} 签名，再调用与真实回调**完全同一条**的
 * {@link PaymentCallbackService#handleCallback} 路径（验签、防重放、金额校验、状态机、时间线全部复用）。
 * 客户端只需轮询 {@code getPayment}，不再持有任何密钥。
 * <p>
 * 边界：
 * <ul>
 *   <li>默认关闭（{@code vso.payment.mock.auto-callback=false}），仅本地/测试显式开启；</li>
 *   <li>只处理 {@code PROCESSING} 且属于 mock 渠道的支付单，已终态一律跳过（幂等）；</li>
 *   <li>不直接写 PAID，仍由签名回调驱动终态（保持 OTD-VSO-REQ-CR-002 INV-006）；</li>
 *   <li>失败仅告警，不影响 {@code initiatePayment} 的返回结果。</li>
 * </ul>
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockPaymentAutoCallbackService {

    static final String MOCK_STATUS_PAID = PaymentStatus.PAID.name();

    private final PaymentRepository paymentRepository;
    private final CallbackSignatureService callbackSignatureService;
    private final PaymentCallbackService paymentCallbackService;
    private final PaymentAdapter paymentAdapter;

    /**
     * 显式开关：留空（默认）表示「跟随渠道」——mock 渠道开启、真实渠道关闭；
     * 显式 true/false 可强制覆盖。避免因外部配置未生效导致 mock 支付永远停在 PROCESSING。
     */
    @Value("${vso.payment.mock.auto-callback:}")
    private String autoCallbackSetting;

    /**
     * 是否参与自动回调：显式配置优先，否则跟随当前装配的支付渠道是否为 mock。
     */
    public boolean isEnabled() {
        if (autoCallbackSetting != null && !autoCallbackSetting.isBlank()) {
            return Boolean.parseBoolean(autoCallbackSetting.trim());
        }
        return paymentAdapter.isMock();
    }

    /**
     * 若开启则模拟一次支付成功回调；返回 null 表示未触发（关闭 / 非 PROCESSING / 支付单不存在）。
     */
    public PaymentCallbackResult triggerIfEnabled(String paymentNo) {
        if (!isEnabled()) {
            return null;
        }
        Payment payment = paymentRepository.findByPaymentNo(paymentNo).orElse(null);
        if (payment == null) {
            log.warn("[MOCK] 自动回调跳过：支付单不存在 paymentNo={}", paymentNo);
            return null;
        }
        if (payment.getPaymentStatus() != PaymentStatus.PROCESSING) {
            log.info("[MOCK] 自动回调跳过：支付单非处理中 paymentNo={}, status={}",
                    paymentNo, payment.getPaymentStatus());
            return null;
        }
        PaymentCallbackResult result = trigger(payment);
        log.info("[MOCK] 自动回调完成：paymentNo={}, code={}, message={}",
                paymentNo, result == null ? null : result.getCode(),
                result == null ? null : result.getMessage());
        return result;
    }

    /**
     * 构造签名回调并复用生产回调处理路径（不新增旁路分支）。
     */
    PaymentCallbackResult trigger(Payment payment) {
        String paymentNo = payment.getPaymentNo();
        String externalTradeNo = payment.getExternalTradeNo() != null
                ? payment.getExternalTradeNo() : "MOCK-" + paymentNo;
        long timestamp = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().replace("-", "");

        Map<String, String> signData = new LinkedHashMap<>();
        signData.put("amount", payment.getPaymentAmount().toPlainString());
        signData.put("orderId", paymentNo);
        signData.put("paySeq", externalTradeNo);
        signData.put("status", MOCK_STATUS_PAID);
        String signature = callbackSignatureService.sign(signData, nonce, timestamp);

        PaymentCallbackCmd cmd = PaymentCallbackCmd.builder()
                .paymentNo(paymentNo)
                .externalTradeNo(externalTradeNo)
                .paymentStage(payment.getPaymentStage())
                .paymentAmount(payment.getPaymentAmount())
                .paymentStatus(MOCK_STATUS_PAID)
                .payTime(LocalDateTime.now())
                .idempotentKey(UUID.randomUUID().toString())
                .build();
        return paymentCallbackService.handleCallback(cmd, signature, timestamp, nonce);
    }
}
