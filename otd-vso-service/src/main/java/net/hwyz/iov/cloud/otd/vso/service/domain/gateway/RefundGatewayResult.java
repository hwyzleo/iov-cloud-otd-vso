package net.hwyz.iov.cloud.otd.vso.service.domain.gateway;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 退款网关受理结果
 *
 * @author VSO Team
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundGatewayResult {

    /** 外部退款单号 */
    private String externalRefundNo;

    /** 网关返回状态：processing / completed / failed */
    private String gatewayStatus;
}
