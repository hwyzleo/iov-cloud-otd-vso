package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售车型价格结果 DTO
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelPriceResult {

    /**
     * 起售价（可售 Variant 的 min(variantPrice)）
     */
    private BigDecimal startingPrice;

    /**
     * 意向金价格（可售 Variant 的 min(earnestMoneyPrice)）
     */
    private BigDecimal earnestMoneyPrice;

    /**
     * 首付价格（可售 Variant 的 min(downPaymentPrice)）
     */
    private BigDecimal downPaymentPrice;

}
