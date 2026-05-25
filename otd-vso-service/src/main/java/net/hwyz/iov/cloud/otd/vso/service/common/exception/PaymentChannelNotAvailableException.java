package net.hwyz.iov.cloud.otd.vso.service.common.exception;

/**
 * 支付渠道不可用异常
 *
 * @author hwyz_leo
 */
public class PaymentChannelNotAvailableException extends VsoBaseException {

    public PaymentChannelNotAvailableException(String channelCode) {
        super(VsoErrorCode.PAYMENT_CHANNEL_NOT_AVAILABLE, "支付渠道[" + channelCode + "]不可用");
    }
}
