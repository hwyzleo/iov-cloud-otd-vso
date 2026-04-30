package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 审核订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class AuditOrderCmd {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 驳回原因（审核驳回时使用）
     */
    private String rejectReason;
}
