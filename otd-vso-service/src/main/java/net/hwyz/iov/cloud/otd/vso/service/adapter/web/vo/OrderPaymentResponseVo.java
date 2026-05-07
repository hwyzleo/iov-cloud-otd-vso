package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentResponseVo {

    private String orderNo;
    private String paymentMerchant;
    private String paymentReference;
    private BigDecimal paymentAmount;
    private Integer paymentDateType;
    private String paymentData;

}
