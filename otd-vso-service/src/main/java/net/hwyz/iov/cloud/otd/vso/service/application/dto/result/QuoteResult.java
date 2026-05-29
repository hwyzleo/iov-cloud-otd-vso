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
     * 总价
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
