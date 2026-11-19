package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 购车合同详情结果（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDetailResult {

    private String contractNo;

    private String contractType;

    private String contractStatus;

    private String signNo;

    /** 仅 SIGNING 时返回当前签署地址 */
    private String signUrl;

    private String fileUrl;

    private Instant generateTime;

    private Instant signTime;

    private Instant effectiveTime;

    private Instant invalidTime;

    private String invalidReason;
}
