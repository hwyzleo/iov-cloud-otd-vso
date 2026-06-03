package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class QuoteResult {
    /**
     * Configuration 编码
     */
    private String configurationCode;

    /**
     * Variant 价格
     */
    private BigDecimal variantPrice;

    /**
     * Option 总价
     */
    private BigDecimal optionTotalPrice;

    /**
     * 总价 = variantPrice + optionTotalPrice
     */
    private BigDecimal totalPrice;

    /**
     * Option 价格明细
     */
    private List<OptionPriceItem> optionPriceBreakdown;

    @Data
    @Builder
    public static class OptionPriceItem {
        private String optionFamilyCode;
        private String optionCode;
        private BigDecimal optionPrice;
    }
}
