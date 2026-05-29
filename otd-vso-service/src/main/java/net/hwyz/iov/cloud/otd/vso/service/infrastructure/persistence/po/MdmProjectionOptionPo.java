package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@TableName("mdm_projection_option")
public class MdmProjectionOptionPo {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("option_code")
    private String optionCode;

    @TableField("option_family_code")
    private String optionFamilyCode;

    @TableField("option_name")
    private String optionName;

    @TableField("mutex_with")
    private String mutexWith;

    @TableField("bundle_with")
    private String bundleWith;

    @TableField("status")
    private String status;
}
