package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaymentRequestVo {

    private String orderNum;
    private Integer orderPaymentPhase;
    private BigDecimal paymentAmount;

}
