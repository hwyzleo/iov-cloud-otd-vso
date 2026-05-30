package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@TableName("mdm_projection_option_family")
public class MdmProjectionOptionFamilyPo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("option_family_code")
    private String optionFamilyCode;

    @TableField("option_family_name")
    private String optionFamilyName;

    @TableField("status")
    private String status;
}
