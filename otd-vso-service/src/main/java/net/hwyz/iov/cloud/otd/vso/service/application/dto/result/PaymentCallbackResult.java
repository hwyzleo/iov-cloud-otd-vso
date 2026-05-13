package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.enums.PaymentCallbackResultCode;

/**
 * 支付回调结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class PaymentCallbackResult {

    private PaymentCallbackResultCode code;
    private String message;

}