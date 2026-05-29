package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@TableName("mdm_projection_configuration")
public class MdmProjectionConfigurationPo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("configuration_code")
    private String configurationCode;

    @TableField("variant_code")
    private String variantCode;

    @TableField("option_codes")
    private String optionCodes;

    @TableField("guide_price")
    private BigDecimal guidePrice;

    @TableField("status")
    private String status;
}
