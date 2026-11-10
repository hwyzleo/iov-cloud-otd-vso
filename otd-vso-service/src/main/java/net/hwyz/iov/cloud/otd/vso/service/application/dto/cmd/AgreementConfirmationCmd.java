package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议确认参数（下单请求内嵌）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementConfirmationCmd {

    private String agreementCode;

    private Integer versionNo;
}
