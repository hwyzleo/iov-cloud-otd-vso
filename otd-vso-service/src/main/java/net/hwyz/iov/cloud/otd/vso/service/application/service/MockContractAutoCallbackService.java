package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ContractCallbackCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PaymentCallbackResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Contract;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.ContractRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock 电子签服务端自回调（OTD-VSO-DSN-CR-014 §5.3）
 * <p>
 * 与 CR-007 的 Mock 支付自回调同构：
 * <ul>
 *     <li>默认关闭（{@code vso.esign.mock.auto-callback=false}），仅本地/测试显式开启；</li>
 *     <li>只处理 SIGNING 合同，构造 SIGNED 回调并复用生产验签、防重放、幂等与状态机路径；</li>
 *     <li>失败仅告警，不绕过生产处理路径；客户端不得持有回调密钥或伪造回调。</li>
 * </ul>
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockContractAutoCallbackService {

    private final ContractRepository contractRepository;
    private final CallbackSignatureService callbackSignatureService;
    private final ContractCallbackService contractCallbackService;

    /**
     * 显式开关：默认 false（Mock 电子签合同停留在 SIGNING，等待合法外部回调）。
     */
    @Value("${vso.esign.mock.auto-callback:false}")
    private boolean autoCallbackEnabled;

    public boolean isEnabled() {
        return autoCallbackEnabled;
    }

    /**
     * 若开启则模拟一次合同签署成功回调；返回 null 表示未触发（关闭 / 非 SIGNING / 合同不存在）。
     */
    public PaymentCallbackResult triggerIfEnabled(String contractNo) {
        if (!isEnabled()) {
            return null;
        }
        Contract contract = contractRepository.findByContractNo(contractNo).orElse(null);
        if (contract == null) {
            log.warn("[MOCK-ESIGN] 自动回调跳过：合同不存在 contractNo={}", contractNo);
            return null;
        }
        if (contract.getContractStatus() != ContractStatus.SIGNING) {
            log.info("[MOCK-ESIGN] 自动回调跳过：合同非签署中 contractNo={}, status={}",
                    contractNo, contract.getContractStatus());
            return null;
        }
        PaymentCallbackResult result = trigger(contract);
        log.info("[MOCK-ESIGN] 自动回调完成：contractNo={}, code={}, message={}",
                contractNo, result == null ? null : result.getCode(),
                result == null ? null : result.getMessage());
        return result;
    }

    /**
     * 构造签名回调并复用生产回调处理路径（不新增旁路分支）。
     */
    PaymentCallbackResult trigger(Contract contract) {
        Instant occurredAt = Instant.now();
        long timestamp = Instant.now().getEpochSecond();
        String nonce = UUID.randomUUID().toString().replace("-", "");

        Map<String, String> signData = new LinkedHashMap<>();
        signData.put("contractNo", contract.getContractNo());
        signData.put("signNo", contract.getSignNo());
        signData.put("status", ContractStatus.SIGNED.name());
        signData.put("occurredAt", occurredAt.toString());
        String signature = callbackSignatureService.sign(signData, nonce, timestamp);

        ContractCallbackCmd cmd = ContractCallbackCmd.builder()
                .contractNo(contract.getContractNo())
                .signNo(contract.getSignNo())
                .status(ContractStatus.SIGNED)
                .occurredAt(occurredAt)
                .idempotentKey(UUID.randomUUID().toString())
                .build();
        return contractCallbackService.handleCallback(cmd, signature, timestamp, nonce);
    }
}
