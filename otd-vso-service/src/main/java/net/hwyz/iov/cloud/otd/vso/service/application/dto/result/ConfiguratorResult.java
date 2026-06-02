package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ConfiguratorResult {
    /**
     * 销售车型编码
     */
    private String saleModelCode;

    /**
     * 销售车型名称
     */
    private String modelName;

    /**
     * 三段式结构：Model → Variant → Option
     */
    private List<ModelItem> models;

    @Data
    @Builder
    public static class ModelItem {
        private String modelCode;
        private String modelName;
        private String marketingImage;
        private String marketingCopy;
        private Integer sortWeight;
        private List<VariantItem> variants;
    }

    @Data
    @Builder
    public static class VariantItem {
        private String variantCode;
        private String variantName;
        private String marketingImage;
        private String marketingCopy;
        private Integer sortWeight;
        private BigDecimal variantPrice;
        private BigDecimal earnestMoneyPrice;
        private BigDecimal downPaymentPrice;
        private List<SelectableFamily> selectableFamilies;
    }

    @Data
    @Builder
    public static class SelectableFamily {
        private String optionFamilyCode;
        private String optionFamilyName;
        private String marketingImage;
        private String marketingDesc;
        private Integer sortWeight;
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
        private List<String> bundleWith;
        private List<String> mutexWith;
    }
}
