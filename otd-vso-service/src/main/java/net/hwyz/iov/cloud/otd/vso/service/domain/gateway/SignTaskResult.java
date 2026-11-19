package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 电子签签署任务结果（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignTaskResult {

    /** 签署任务号（写入 vso_contract.sign_no） */
    private String signNo;

    /** 签署地址（短期有效，不持久化；日志不得输出完整地址） */
    private String signUrl;
}
