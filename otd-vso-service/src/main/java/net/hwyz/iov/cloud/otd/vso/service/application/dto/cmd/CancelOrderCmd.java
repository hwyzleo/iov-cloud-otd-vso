package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 取消/关闭订单命令
 *
 * @author VSO Team
 */
@Data
@Builder
public class CancelOrderCmd {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 操作人ID
     */
    private String operatorId;

    /**
     * 原因
     */
    private String reason;

    /**
     * 操作类型：CANCEL-取消，CLOSE-关闭
     */
    private String operateType;
}
