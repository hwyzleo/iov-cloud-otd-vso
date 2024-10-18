package net.hwyz.iov.cloud.otd.vso.api.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 支付渠道枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum PaymentChannel {

    /** 银联 **/
    UNION_PAY,
    /** 微信 **/
    WECHAT,
    /** 支付宝 **/
    ALIPAY;
    public static PaymentChannel valOf(String val) {
        return Arrays.stream(PaymentChannel.values())
                .filter(paymentChannel -> paymentChannel.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
