package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vso_wishlist")
public class WishlistPo extends BasePo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("wishlist_id")
    private String wishlistId;

    @TableField("user_id")
    private String userId;

    @TableField("sale_model")
    private String saleModel;

    @TableField("build_config_code")
    private String buildConfigCode;

    @TableField("wishlist_name")
    private String wishlistName;

    @TableField("status")
    private String status;

    /**
     * 销售车型编码
     */
    @TableField("sale_model_code")
    private String saleModelCode;

    /**
     * Model 编码
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * Variant 编码
     */
    @TableField("variant_code")
    private String variantCode;

    /**
     * Configuration 编码
     */
    @TableField("configuration_code")
    private String configurationCode;

    /**
     * OptionCode 列表（JSON）
     */
    @TableField("option_codes")
    private String optionCodes;

    /**
     * OptionCode 排序后哈希
     */
    @TableField("option_codes_hash")
    private String optionCodesHash;

    /**
     * 失效原因枚举
     */
    @TableField("invalid_reason")
    private String invalidReason;

}