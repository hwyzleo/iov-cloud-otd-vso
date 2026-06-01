package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.sql.Timestamp;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * MDM Model 投影 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mdm_projection_model")
public class MdmProjectionModelPo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Model 编码
     */
    @TableField("model_code")
    private String modelCode;

    /**
     * Model 名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 所属 Carline 编码
     */
    @TableField("carline_code")
    private String carlineCode;

    /**
     * 下属 Variant 编码列表（JSON）
     */
    @TableField("variant_codes")
    private String variantCodes;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Timestamp createTime;

    /**
     * 修改时间
     */
    @TableField("modify_time")
    private Timestamp modifyTime;
}
