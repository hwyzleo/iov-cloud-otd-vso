package net.hwyz.iov.cloud.otd.vso.service.common.exception;

import lombok.extern.slf4j.Slf4j;

/**
 * 支付渠道不可用异常
 *
 * @author hwyz_leo
 */
@Slf4j
public class PaymentChannelNotAvailableException extends VsoBaseException {

    public PaymentChannelNotAvailableException(String channelCode) {
        super(ERROR_CODE_PAYMENT_CHANNEL_NOT_AVAILABLE);
        log.warn("支付渠道[{}]不可用", channelCode);
    }

}