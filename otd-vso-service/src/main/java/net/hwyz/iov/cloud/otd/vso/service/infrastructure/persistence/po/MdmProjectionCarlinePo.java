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
 * MDM Carline 投影 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2026-06-01
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("mdm_projection_carline")
public class MdmProjectionCarlinePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Carline 编码
     */
    @TableField("carline_code")
    private String carlineCode;

    /**
     * Carline 名称
     */
    @TableField("carline_name")
    private String carlineName;

    /**
     * 下属 Model 编码列表（JSON）
     */
    @TableField("model_codes")
    private String modelCodes;

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
