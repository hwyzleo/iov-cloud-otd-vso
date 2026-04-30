package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 提交订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class SubmitOrderCmd {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 操作人ID
     */
    private String operatorId;
}
