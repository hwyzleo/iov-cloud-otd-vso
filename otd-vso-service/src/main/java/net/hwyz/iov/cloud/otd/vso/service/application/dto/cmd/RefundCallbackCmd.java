package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款回调命令（设计 §11.6）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundCallbackCmd {

    private String refundNo;

    private String refundItemId;

    private String externalRefundNo;

    private BigDecimal refundAmount;

    /** processing / completed / failed */
    private String refundStatus;

    private LocalDateTime refundTime;

    private String idempotentKey;

    private String failReason;
}
