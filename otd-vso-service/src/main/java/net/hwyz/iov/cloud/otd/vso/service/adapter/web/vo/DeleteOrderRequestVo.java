package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 删除订单请求 VO
 *
 * @author VSO Team
 */
@Data
public class DeleteOrderRequestVo {

    /**
     * 订单号（可选，用于校验）
     */
    private String orderNo;

    /**
     * 删除原因
     */
    private String reason;

    /**
     * 是否因合规要求删除（默认 false）
     */
    private Boolean complianceFlag;
}
