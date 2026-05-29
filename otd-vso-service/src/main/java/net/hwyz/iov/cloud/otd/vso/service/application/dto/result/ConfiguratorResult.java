package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ConfiguratorResult {
    /**
     * Variant 编码
     */
    private String variantCode;

    /**
     * Variant 名称
     */
    private String variantName;

    /**
     * Model 编码
     */
    private String modelCode;

    /**
     * Model 名称
     */
    private String modelName;

    /**
     * 标配 options
     */
    private List<StandardOption> variantStandardOptions;

    /**
     * 可选 families
     */
    private List<SelectableFamily> selectableFamilies;

    /**
     * 起售价
     */
    private BigDecimal basePrice;

    /**
     * 意向金价格
     */
    private BigDecimal earnestMoneyPrice;

    /**
     * 定金价格
     */
    private BigDecimal downPaymentPrice;

    @Data
    @Builder
    public static class StandardOption {
        private String optionCode;
        private String optionName;
    }

    @Data
    @Builder
    public static class SelectableFamily {
        private String optionFamilyCode;
        private String optionFamilyName;
        private Integer sortWeight;
        private Boolean required;
        private List<OptionItem> options;
    }

    @Data
    @Builder
    public static class OptionItem {
        private String optionCode;
        private String optionName;
        private String saleStatus;
        private BigDecimal price;
        private String image;
        private String marketingCopy;
        private List<String> badges;
        private List<String> bundleWith;
        private List<String> mutexWith;
    }
}
