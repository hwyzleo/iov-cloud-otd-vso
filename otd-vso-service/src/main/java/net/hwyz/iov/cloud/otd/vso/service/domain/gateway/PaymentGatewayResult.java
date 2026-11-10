package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 支付网关受理结果
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResult {

    /** 外部交易单号 */
    private String externalTradeNo;

    /** 收银台商户号 / 收款方 */
    private String paymentMerchant;

    /** 收银台跳转参数 */
    private String paymentReference;

    /** 网关返回状态：PROCESSING / PAID / PAYMENT_FAILED / CANCELLED */
    private String gatewayStatus;
}
