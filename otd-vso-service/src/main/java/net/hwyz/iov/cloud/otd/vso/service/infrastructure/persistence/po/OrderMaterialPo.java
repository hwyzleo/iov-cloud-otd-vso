package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单资料表持久化对象
 */
@Data
@TableName("vso_order_material")
public class OrderMaterialPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("material_id")
    private String materialId;

    @TableField("order_id")
    private String orderId;

    @TableField("material_type")
    private String materialType;

    @TableField("material_name")
    private String materialName;

    @TableField("material_status")
    private String materialStatus;

    @TableField("current_version_no")
    private Integer currentVersionNo;

    @TableField("submitter_role")
    private String submitterRole;

    @TableField("submitter_id")
    private String submitterId;

    @TableField("review_comment")
    private String reviewComment;

    @TableField("timeout_flag")
    private Integer timeoutFlag;

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
