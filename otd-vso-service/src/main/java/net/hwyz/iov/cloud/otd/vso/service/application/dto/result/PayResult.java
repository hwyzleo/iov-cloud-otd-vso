package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PayResult {
    private String orderNum;
    private String paymentMerchant;
    private String paymentReference;
    private BigDecimal paymentAmount;
}
