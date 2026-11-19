package net.hwyz.iov.cloud.otd.vso.service.infrastructure.gateway;

import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.ContractGenerationResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignAdapter;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.EsignSignStatusResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.gateway.SignTaskResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock 电子签适配器（OTD-VSO-DSN-CR-014）
 * <p>
 * 本期无真实电子签渠道依赖，Mock 仅生成 {@code mock://esign/...} 地址，
 * 不宣称真实 PDF、CA 实名或短信签署；未来真实渠道可替换本 Bean 而不改应用服务。
 * 签署终态仍由验签、防重放、任务匹配与状态机校验后的回调产生，Mock 不自证成功。
 *
 * @author VSO Team
 */
@Slf4j
@Component
public class MockEsignAdapter implements EsignAdapter {

    private static final String MOCK_FILE_PREFIX = "mock://esign/contract/";
    private static final String MOCK_SIGN_PREFIX = "mock://esign/sign/";
    private static final String MOCK_EXTERNAL_PREFIX = "MOCK-ESIGN-";

    @Override
    public ContractGenerationResult generateContract(String orderId, String contractType, String templateId) {
        String externalContractNo = MOCK_EXTERNAL_PREFIX + compactUuid();
        String fileUrl = MOCK_FILE_PREFIX + orderId + "/" + externalContractNo;
        log.info("[MOCK-ESIGN] 生成合同：orderId={}, contractType={}, templateId={}, externalContractNo={}",
                orderId, contractType, templateId, externalContractNo);
        return ContractGenerationResult.builder()
                .externalContractNo(externalContractNo)
                .fileUrl(fileUrl)
                .generateTime(LocalDateTime.now())
                .build();
    }

    @Override
    public SignTaskResult createSignTask(String contractId, String signerId) {
        String signNo = "SIGN" + compactUuid();
        String signUrl = MOCK_SIGN_PREFIX + signNo;
        // 签署地址不落库、不输出完整值，仅返回给本人客户端
        log.info("[MOCK-ESIGN] 创建签署任务：contractId={}, signerId={}, signNo={}", contractId, signerId, signNo);
        return SignTaskResult.builder().signNo(signNo).signUrl(signUrl).build();
    }

    @Override
    public EsignSignStatusResult querySignStatus(String signNo) {
        log.info("[MOCK-ESIGN] 查询签署状态：signNo={}", signNo);
        return EsignSignStatusResult.builder()
                .signNo(signNo)
                .status(ContractStatus.SIGNING)
                .signUrl(MOCK_SIGN_PREFIX + signNo)
                .build();
    }

    private String compactUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
