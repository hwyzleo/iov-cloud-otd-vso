package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 心愿单列表结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class WishlistListResult {

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
     * 展示名称（销售车型名 + 车型营销名 + 版本营销名）
     */
    private String displayName;

    /**
     * 销售车型描述（用户选择的 option 营销名称拼接）
     */
    private String saleModelDesc;

    /**
     * 销售车型图片集（用户选择的 option 营销图片数组）
     */
    private List<String> saleModelImages;

    /**
     * 总价格（版本价格 + 选项价格）
     */
    private BigDecimal totalPrice;

}
