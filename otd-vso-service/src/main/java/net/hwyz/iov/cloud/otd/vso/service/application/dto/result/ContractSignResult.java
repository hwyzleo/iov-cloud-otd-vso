package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 发起合同签署结果（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractSignResult {

    private String contractNo;

    private String contractType;

    private String contractStatus;

    private String signNo;

    /** 签署地址（短期有效，不落库；日志不得输出完整值） */
    private String signUrl;

    private String fileUrl;

    private Instant generateTime;
}
