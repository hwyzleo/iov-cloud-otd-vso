package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 电子签合同生成结果（OTD-VSO-DSN-CR-014）
 * <p>
 * 结构化承载外部合同文件标识与地址，禁止用字符串拼接承载多个返回值。
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractGenerationResult {

    /** 外部电子签合同/文件标识（写入 vso_contract.external_contract_no） */
    private String externalContractNo;

    /** 合同文件地址（Mock 为 mock://esign/...，不宣称真实 PDF 或 CA 签署） */
    private String fileUrl;

    /** 生成时间 */
    private LocalDateTime generateTime;
}
