package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.framework.mysql.po.BasePo;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("vso_wishlist")
public class WishlistPo extends BasePo {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("wishlist_id")
    private String wishlistId;

    @TableField("user_id")
    private String userId;

    @TableField("sale_model")
    private String saleModel;

    @TableField("build_config_code")
    private String buildConfigCode;

    @TableField("wishlist_name")
    private String wishlistName;

    @TableField("status")
    private String status;

}