package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("tb_sale_model_option_policy")
public class SaleModelOptionPolicyPo extends BasePo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sale_model_code")
    private String saleModelCode;

    @TableField("option_code")
    private String optionCode;

    @TableField("option_family_code")
    private String optionFamilyCode;

    @TableField("sale_status")
    private String saleStatus;

    @TableField("option_price")
    private BigDecimal optionPrice;

    @TableField("available_regions")
    private String availableRegions;

    @TableField("channels")
    private String channels;

    @TableField("bundle_with")
    private String bundleWith;

    @TableField("mutex_with")
    private String mutexWith;

    @TableField("marketing_title")
    private String marketingTitle;

    @TableField("marketing_image")
    private String marketingImage;

    @TableField("sort_weight")
    private Integer sortWeight;

    @TableField("effective_from")
    private Timestamp effectiveFrom;

    @TableField("effective_to")
    private Timestamp effectiveTo;

    @TableField("description")
    private String description;
}
