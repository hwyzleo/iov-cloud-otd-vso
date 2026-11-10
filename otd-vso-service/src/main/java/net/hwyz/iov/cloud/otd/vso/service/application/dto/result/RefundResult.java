package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退款申请与进度结果（设计 §11.6）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResult {

    private String refundNo;
    private String orderNo;
    /**
     * 退款场景（未锁单 / 锁单 / 超额），运营核对手续费规则的依据
     */
    private String refundScene;
    private BigDecimal refundAmount;
    private BigDecimal refundFee;
    private String refundStatus;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    /**
     * 退款完成时间
     */
    private LocalDateTime refundTime;
    /**
     * 失败原因
     */
    private String failReason;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String refundItemId;
        private String paymentNo;
        private BigDecimal refundAmount;
        private String refundStatus;
        private String externalRefundNo;
        private String failReason;
    }
}
