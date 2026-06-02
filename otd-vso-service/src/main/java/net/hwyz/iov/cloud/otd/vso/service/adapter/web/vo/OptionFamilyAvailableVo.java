package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 可选 OptionCode 视图对象（按 OptionFamily 分组）
 * 用于销售策略页展示可选 OptionCode 列表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionFamilyAvailableVo {

    /**
     * 选项族编码
     */
    private String optionFamilyCode;

    /**
     * 选项族名称（MDM 默认名称）
     */
    private String optionFamilyName;

    /**
     * 营销标题（自定义，为空时使用 optionFamilyName）
     */
    private String marketingTitle;

    /**
     * 营销图片
     */
    private String marketingImage;

    /**
     * 营销描述
     */
    private String marketingDesc;

    /**
     * 该族下的 OptionCode 列表
     */
    private List<OptionAvailableVo> options;

    /**
     * OptionCode 可选视图对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionAvailableVo {

        /**
         * 策略 ID（inPolicy=true 时有值）
         */
        private Long id;

        /**
         * 选项编码
         */
        private String optionCode;

        /**
         * 选项名称
         */
        private String optionName;

        /**
         * 是否已有销售策略
         */
        private Boolean inPolicy;

        /**
         * 销售状态（active/off_shelf/coming_soon，无策略时为 null）
         */
        private String saleStatus;

        /**
         * 价格（无策略时为 null）
         */
        private BigDecimal optionPrice;

        /**
         * 营销标题（无策略时为 null）
         */
        private String marketingTitle;
    }
}
