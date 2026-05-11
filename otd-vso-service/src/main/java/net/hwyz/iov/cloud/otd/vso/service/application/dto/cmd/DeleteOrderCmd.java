package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Data;

/**
 * 删除订单命令
 *
 * @author VSO Team
 */
@Data
public class DeleteOrderCmd {

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 订单号（可选，用于校验）
     */
    private String orderNo;

    /**
     * 操作人 ID
     */
    private String operatorId;

    /**
     * 操作人角色
     */
    private String operatorRole;

    /**
     * 删除原因
     */
    private String reason;

    /**
     * 是否因合规要求删除（默认 false）
     */
    private Boolean complianceFlag;
}
