package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import net.hwyz.iov.cloud.tsp.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 销售车型配置 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-11
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_sale_model_config")
public class SaleModelConfigPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 销售代码
     */
    @TableField("sale_code")
    private String saleCode;

    /**
     * 销售车型配置类型
     */
    @TableField("type")
    private String type;

    /**
     * 销售车型配置类型代码
     */
    @TableField("type_code")
    private String typeCode;

    /**
     * 销售车型配置类型名称
     */
    @TableField("type_name")
    private String typeName;

    /**
     * 销售车型配置类型价格
     */
    @TableField("type_price")
    private BigDecimal typePrice;

    /**
     * 销售车型配置类型图片
     */
    @TableField("type_image")
    private String typeImage;

    /**
     * 销售车型配置类型描述
     */
    @TableField("type_desc")
    private String typeDesc;

    /**
     * 销售车型配置类型参数
     */
    @TableField("type_param")
    private String typeParam;

    /**
     * 是否启用
     */
    @TableField("enable")
    private Boolean enable;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;
}
