package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单版本差异表持久化对象
 */
@Data
@TableName("vso_order_version_diff")
public class OrderVersionDiffPo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_version_diff_id")
    private String orderVersionDiffId;

    @TableField("order_version_id")
    private String orderVersionId;

    @TableField("field_name")
    private String fieldName;

    @TableField("field_label")
    private String fieldLabel;

    @TableField("before_value")
    private String beforeValue;

    @TableField("after_value")
    private String afterValue;

    @TableField("change_category")
    private String changeCategory;

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
