package net.hwyz.iov.cloud.otd.vso.service.application.dto.enums;

import lombok.AllArgsConstructor;

/**
 * 支付回调结果码
 *
 * @author VSO Team
 */
@AllArgsConstructor
public enum PaymentCallbackResultCode {

    SUCCESS("回调处理成功"),
    DUPLICATE("该回调已处理"),
    FAIL("回调处理失败");

    private final String description;

    public String getDescription() {
        return this.description;
    }

}