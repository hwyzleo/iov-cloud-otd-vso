package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 定金下单结果
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownPaymentOrderResult {

    private String orderNo;
    private BigDecimal downPaymentAmount;
    private List<PaymentChannelInfo> paymentChannels;
    private Instant expireTime;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentChannelInfo {
        private PaymentChannel channelCode;
        private String channelName;
        private Boolean isDefault;
    }

}