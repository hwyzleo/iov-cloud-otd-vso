package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.Builder;
import lombok.Data;

/**
 * 完成订单命令（MPT 人工办结，OTD-VSO-DSN-CR-016）
 * <p>
 * 不接收操作角色、操作来源、目标状态与办结时间：应用层固定写入 mpt_operator/mpt 和 900。
 *
 * @author VSO Team
 */
@Data
@Builder
public class CompleteOrderCmd {

    /**
     * 订单业务 ID
     */
    private String orderId;

    /**
     * 操作人 ID（只取 MPT 安全上下文）
     */
    private String operatorId;

    /**
     * 办结备注（可选，最长 255）
     */
    private String completionRemark;
}
