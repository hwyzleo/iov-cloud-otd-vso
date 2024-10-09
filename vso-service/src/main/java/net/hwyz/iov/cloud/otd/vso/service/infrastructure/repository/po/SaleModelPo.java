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
 * 销售车型 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-08
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_sale_model")
public class SaleModelPo extends BasePo {

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
     * 销售车型类型
     */
    @TableField("sale_model_type")
    private String saleModelType;

    /**
     * 销售车型类型代码
     */
    @TableField("sale_model_type_code")
    private String saleModelTypeCode;

    /**
     * 销售名称
     */
    @TableField("sale_name")
    private String saleName;

    /**
     * 销售价格
     */
    @TableField("sale_price")
    private BigDecimal salePrice;

    /**
     * 销售图片
     */
    @TableField("sale_image")
    private String saleImage;

    /**
     * 销售描述
     */
    @TableField("sale_desc")
    private String saleDesc;

    /**
     * 销售参数
     */
    @TableField("sale_param")
    private String saleParam;

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
