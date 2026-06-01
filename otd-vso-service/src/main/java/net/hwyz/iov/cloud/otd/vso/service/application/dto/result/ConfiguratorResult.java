package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ConfiguratorResult {
    /**
     * Carline 编码
     */
    private String carlineCode;

    /**
     * Carline 名称
     */
    private String carlineName;

    /**
     * 三段式结构：Carline → Model → Variant
     */
    private List<ModelItem> models;

    @Data
    @Builder
    public static class ModelItem {
        private String modelCode;
        private String modelName;
        private List<VariantItem> variants;
    }

    @Data
    @Builder
    public static class VariantItem {
        private String variantCode;
        private String variantName;
        private BigDecimal variantPrice;
        private BigDecimal earnestMoneyPrice;
        private BigDecimal downPaymentPrice;
    }

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
