package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.ContractStatus;

/**
 * 电子签签署状态查询结果（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EsignSignStatusResult {

    /** 签署任务号 */
    private String signNo;

    /** 渠道侧签署状态（只作参考，合同事实以 vso_contract 为准） */
    private ContractStatus status;

    /** 当前签署地址（按 signNo 查询/重建，不持久化） */
    private String signUrl;
}
