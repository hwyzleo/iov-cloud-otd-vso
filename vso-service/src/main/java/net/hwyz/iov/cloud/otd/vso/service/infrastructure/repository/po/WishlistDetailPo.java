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
 * 心愿单详情 数据对象
 * </p>
 *
 * @author hwyz_leo
 * @since 2024-10-10
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_wishlist_detail")
public class WishlistDetailPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 心愿单ID
     */
    @TableField("wishlist_id")
    private Long wishlistId;

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
}
