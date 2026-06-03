package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 心愿单详情 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDetailVo {

    private String wishlistId;
    private String saleModelCode;
    private String modelCode;
    private String variantCode;
    private String configurationCode;
    private List<String> optionCodes;
    private Date createTime;
    private Date modifyTime;
    private String invalidReason;

    /**
     * 销售车型名称
     */
    private String saleModelName;

    /**
     * 车型营销名称
     */
    private String modelMarketingName;

    /**
     * 版本营销名称
     */
    private String variantMarketingName;

    /**
     * 版本价格
     */
    private BigDecimal variantPrice;

    /**
     * 销售车型图片集（用户选择的 option 营销图片数组）
     */
    private List<String> saleModelImages;

    /**
     * 总价格（版本价格 + 选项价格）
     */
    private BigDecimal totalPrice;

    /**
     * 选项详情列表
     */
    private List<OptionDetailVo> optionDetails;

    /**
     * 选项详情 VO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDetailVo {
        private String optionFamilyCode;
        private String optionCode;
        private String marketingTitle;
        private BigDecimal optionPrice;
        private String marketingImage;
    }

}
