package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发起合同签署命令（OTD-VSO-DSN-CR-014）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateContractSignCmd {

    /** 账号 ID（只取安全上下文，不接受客户端传入） */
    private String accountId;

    /** 订单号 */
    private String orderNo;
}
