package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * 重新发起支付结果（OTD-VSO-DSN-CR-005）
 *
 * @author VSO Team
 */
@Data
@Builder
public class ReopenPaymentResult {

    /** 订单号 */
    private String orderNo;

    /** 恢复后的订单状态值（EARNEST_MONEY_UNPAID = 200） */
    private Integer orderState;

    /** 本次恢复后已使用的恢复次数（含本次） */
    private Integer reopenCount;

    /** 本次支付窗口截止时间（按 CR-001 输出 epoch 毫秒） */
    private Instant expireTime;

}
