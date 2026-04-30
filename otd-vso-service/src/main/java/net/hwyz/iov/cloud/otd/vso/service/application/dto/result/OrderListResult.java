package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * 订单列表结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderListResult {

    /**
     * 订单 ID
     */
    private String orderId;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 展示名称
     */
    private String displayName;
}
