package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantPolicyAvailableVo {

    private String variantCode;

    private String variantName;

    private String modelCode;

    private String modelName;

    private String status;

    private Boolean inPolicy;

    private String saleStatus;

    private BigDecimal variantPrice;

    private BigDecimal earnestMoneyPrice;

    private BigDecimal downPaymentPrice;
}
