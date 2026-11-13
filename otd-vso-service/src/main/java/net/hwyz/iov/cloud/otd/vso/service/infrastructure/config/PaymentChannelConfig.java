package net.hwyz.iov.cloud.otd.vso.service.infrastructure.config;

import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.api.enums.PaymentChannel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 支付渠道配置
 *
 * @author hwyz_leo
 */
@Data
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentChannelConfig {

    private List<ChannelInfo> channels = new ArrayList<>();
    private PaymentChannel defaultChannel = PaymentChannel.WECHAT;
    private Integer smallOrderTimeoutMinutes = 30;
    private Integer downPaymentTimeoutMinutes = 60;

    /**
     * 处理中支付单失联宽限分钟数（OTD-VSO-DSN-CR-005）
     * <p>
     * PROCESSING 支付单超过 expireTime + 该值视为失联，允许关闭并重建。
     */
    private Integer processingStaleMinutes = 30;

    /**
     * 处理中支付单失联宽限分钟数（缺失或 <=0 按 30 处理）
     */
    public int processingStaleMinutesOrDefault() {
        return processingStaleMinutes == null || processingStaleMinutes <= 0 ? 30 : processingStaleMinutes;
    }

    @Data
    public static class ChannelInfo {
        private PaymentChannel code;
        private String name;
        private Boolean enabled = true;
        private Integer sort = 0;
    }

    public List<ChannelInfo> getEnabledChannels() {
        return channels.stream()
                .filter(c -> Boolean.TRUE.equals(c.getEnabled()))
                .sorted((a, b) -> (a.getSort() != null ? a.getSort() : 0) - (b.getSort() != null ? b.getSort() : 0))
                .toList();
    }

    public ChannelInfo getDefaultChannelInfo() {
        return channels.stream()
                .filter(c -> c.getCode() == defaultChannel && Boolean.TRUE.equals(c.getEnabled()))
                .findFirst()
                .orElse(getEnabledChannels().stream().findFirst().orElse(null));
    }

    public boolean isChannelEnabled(PaymentChannel channel) {
        return channels.stream()
                .anyMatch(c -> c.getCode() == channel && Boolean.TRUE.equals(c.getEnabled()));
    }

}