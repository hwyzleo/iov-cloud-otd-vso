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