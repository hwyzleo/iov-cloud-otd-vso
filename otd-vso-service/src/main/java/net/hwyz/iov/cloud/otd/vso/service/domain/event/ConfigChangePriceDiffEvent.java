package net.hwyz.iov.cloud.otd.vso.service.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 改配价格差额领域事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigChangePriceDiffEvent {

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private String accountId;

    /**
     * 价格差额（正数表示补款，负数表示退款）
     */
    private BigDecimal priceDifference;

    /**
     * 新配置版本号
     */
    private Integer newConfigVersionNo;

    /**
     * 补款单号（差额>0时有值）
     */
    private String supplementaryNo;

    /**
     * 退款任务单号（差额<0时有值）
     */
    private String refundTaskNo;
}
