package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 重新发起支付命令（OTD-VSO-DSN-CR-005）
 *
 * @author VSO Team
 */
@Data
@Builder
public class ReopenPaymentCmd {

    /** 账号业务 ID（由 SecurityContext 注入，不得来自请求体） */
    private String accountId;

    /** 订单号 */
    private String orderNo;

}
