package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 小订单转正式订单转化关系表持久化对象
 */
@Data
@TableName("vso_order_transform")
public class OrderTransformPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("transform_id")
    private String transformId;

    @TableField("small_order_id")
    private String smallOrderId;

    @TableField("small_order_no")
    private String smallOrderNo;

    @TableField("formal_order_id")
    private String formalOrderId;

    @TableField("formal_order_no")
    private String formalOrderNo;

    @TableField("transform_time")
    private LocalDateTime transformTime;

    @TableField("transform_user_id")
    private String transformUserId;

    @TableField("transform_user_role")
    private String transformUserRole;

    @TableField("transform_scene")
    private String transformScene;

    @TableField("transform_remark")
    private String transformRemark;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "modify_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime modifyTime;

    @TableField(value = "modify_by", fill = FieldFill.INSERT_UPDATE)
    private Long modifyBy;

    @TableField(value = "row_version", fill = FieldFill.INSERT)
    private Integer rowVersion;

    @TableField(value = "row_valid", fill = FieldFill.INSERT)
    private Integer rowValid;
}
