package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.shared.Money;

import java.time.LocalDateTime;

/**
 * 补款任务信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplementaryPaymentInfo {

    private String supplementaryNo;
    private Money amount;
    private LocalDateTime expireTime;
}
