package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

/**
 * <p>
 * Variant 销售策略 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vso_sale_model_variant_policy")
public class SaleModelVariantPolicyPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 销售状态：active/off_shelf
     */
    @TableField("sale_status")
    private String saleStatus;

    /**
     * Variant 价格（必填）
     */
    @TableField("variant_price")
    private BigDecimal variantPrice;

    /**
     * 意向金价格
     */
    @TableField("earnest_money_price")
    private BigDecimal earnestMoneyPrice;

    /**
     * 定金价格
     */
    @TableField("down_payment_price")
    private BigDecimal downPaymentPrice;

    /**
     * 可售区域列表，为空表示全国
     */
    @TableField("available_regions")
    private String availableRegions;

    /**
     * 可售渠道列表，为空表示全渠道
     */
    @TableField("channels")
    private String channels;

    /**
     * 营销名称
     */
    @TableField("marketing_name")
    private String marketingName;

    /**
     * 营销图片 URL
     */
    @TableField("marketing_image")
    private String marketingImage;

    /**
     * 营销文案
     */
    @TableField("marketing_copy")
    private String marketingCopy;

    /**
     * 排序权重
     */
    @TableField("sort_weight")
    private Integer sortWeight;

    /**
     * 生效开始时间
     */
    @TableField("effective_from")
    private Timestamp effectiveFrom;

    /**
     * 生效结束时间
     */
    @TableField("effective_to")
    private Timestamp effectiveTo;

    /**
     * 备注
     */
    @TableField("description")
    private String description;
}
