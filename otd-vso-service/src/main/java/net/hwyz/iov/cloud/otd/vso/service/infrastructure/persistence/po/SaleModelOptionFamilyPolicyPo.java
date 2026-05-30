package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vso_sale_model_option_family_policy")
public class SaleModelOptionFamilyPolicyPo extends BasePo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sale_model_code")
    private String saleModelCode;

    @TableField("option_family_code")
    private String optionFamilyCode;

    @TableField("marketing_title")
    private String marketingTitle;

    @TableField("marketing_image")
    private String marketingImage;

    @TableField("marketing_desc")
    private String marketingDesc;

    @TableField("sort_weight")
    private Integer sortWeight;
}
