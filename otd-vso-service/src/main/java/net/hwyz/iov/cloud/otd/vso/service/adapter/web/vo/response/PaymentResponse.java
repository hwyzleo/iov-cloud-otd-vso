package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * M端支付响应
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String orderNum;
    private String paymentMerchant;
    private String paymentReference;
    private BigDecimal paymentAmount;
    private Integer paymentData;
    private String paymentData2;

}
