package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.math.BigDecimal;

/**
 * 销售车型基础车型关联 数据对象
 *
 * @author hwyz_leo
 * @since 2026-05-07
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("vso_sale_model_base_model")
public class SaleModelBaseModelPo extends BasePo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("sale_code")
    private String saleModelCode;

    @TableField("base_model_code")
    private String baseModelCode;

    @TableField("base_model_name")
    private String baseModelName;

    @TableField("base_model_image")
    private String baseModelImage;

    @TableField("base_model_price")
    private BigDecimal baseModelPrice;

    @TableField("base_model_desc")
    private String baseModelDesc;

    @TableField("base_model_param")
    private String baseModelParam;

    @TableField("enable")
    private Boolean enable;

    @TableField("sort")
    private Integer sort;
}