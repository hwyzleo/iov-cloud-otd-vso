package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 意向金转定金响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarnestToDownResponseVo {

    private String orderNo;
    private String orderType;
    private String orderState;
    private SupplementaryPaymentVo supplementaryPayment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplementaryPaymentVo {
        private String supplementaryNo;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime expireTime;
    }
}
