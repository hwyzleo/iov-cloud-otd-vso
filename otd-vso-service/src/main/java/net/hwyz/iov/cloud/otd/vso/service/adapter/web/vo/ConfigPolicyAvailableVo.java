package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 可选 Configuration 视图对象
 * 用于销售策略页展示 MDM 投影中的 Configuration 列表，并标注是否在白名单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigPolicyAvailableVo {

    /**
     * Configuration 编码
     */
    private String configurationCode;

    /**
     * 所属 Variant 编码
     */
    private String variantCode;

    /**
     * 包含的 OptionCode 列表
     */
    private List<String> optionCodes;

    /**
     * 指导价
     */
    private BigDecimal guidePrice;

    /**
     * 是否在白名单中
     */
    private Boolean inWhitelist;

    /**
     * 白名单状态（active/off_shelf，不在白名单时为 null）
     */
    private String policyStatus;
}
