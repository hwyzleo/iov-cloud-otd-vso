package net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.*;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

/**
 * <p>
 * 订单车型配置 数据对象
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
@TableName("tb_order_model_config")
public class OrderModelConfigPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单编码
     */
    @TableField("order_num")
    private String orderNum;

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
}
