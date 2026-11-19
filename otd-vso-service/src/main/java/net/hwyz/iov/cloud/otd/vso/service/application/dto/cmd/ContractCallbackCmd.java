package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;

import java.time.Instant;

/**
 * 电子签回调命令（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractCallbackCmd {

    /** 合同号 */
    private String contractNo;

    /** 签署任务号 */
    private String signNo;

    /** 回调状态：SIGNING / SIGNED / EFFECTIVE / INVALID */
    private ContractStatus status;

    /** 渠道事件发生时间 */
    private Instant occurredAt;

    /** 幂等键 */
    private String idempotentKey;

    /** 失效原因（INVALID 时可选） */
    private String failReason;
}
