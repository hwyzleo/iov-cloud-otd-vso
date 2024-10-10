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
 * 心愿单 数据对象
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
@TableName("tb_wishlist")
public class WishlistPo extends BasePo {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 账号唯一ID
     */
    @TableField("account_id")
    private String accountId;

    /**
     * 销售代码
     */
    @TableField("sale_code")
    private String saleCode;

    /**
     * 销售车型代码
     */
    @TableField("sale_model_code")
    private String saleModelCode;

    /**
     * 意向金
     */
    @TableField("intention_money")
    private BigDecimal intentionMoney;

    /**
     * 是否转订单
     */
    @TableField("is_order")
    private Boolean isOrder;
}
