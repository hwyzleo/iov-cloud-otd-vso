package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import net.hwyz.iov.cloud.tsp.framework.mysql.po.BasePo;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * <p>
 * 销售车型 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-14
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
     * 销售车型名称
     */
    @TableField("model_name")
    private String modelName;

    /**
     * 销售车型相关参数
     */
    @TableField("parameters")
    private String parameters;

    /**
     * 销售车型图片集
     */
    @TableField("images")
    private String images;

    /**
     * 是否允许意向金（小定）
     */
    @TableField("earnest_money")
    private Boolean earnestMoney;

    /**
     * 是否允许定金（大定）
     */
    @TableField("down_payment")
    private Boolean downPayment;

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
