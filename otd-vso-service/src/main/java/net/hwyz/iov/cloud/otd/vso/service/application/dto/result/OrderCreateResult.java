package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

/**
 * 订单创建结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class OrderCreateResult {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;
}
