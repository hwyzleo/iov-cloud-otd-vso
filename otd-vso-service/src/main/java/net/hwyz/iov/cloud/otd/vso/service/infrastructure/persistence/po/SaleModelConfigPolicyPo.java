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
@TableName("vso_sale_model_config_policy")
public class SaleModelConfigPolicyPo extends BasePo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sale_model_code")
    private String saleModelCode;

    @TableField("model_code")
    private String modelCode;

    @TableField("variant_code")
    private String variantCode;

    @TableField("configuration_code")
    private String configurationCode;

    @TableField("status")
    private String status;

    @TableField("description")
    private String description;
}
