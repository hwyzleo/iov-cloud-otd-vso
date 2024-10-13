package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.contract.Wishlist;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 心愿单响应
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WishlistResponse extends Wishlist {
    /**
     * 销售车型配置名称
     */
    private Map<String, String> saleModelConfigName;
    /**
     * 销售车型配置价格
     */
    private Map<String, BigDecimal> saleModelConfigPrice;
    /**
     * 销售车型图片
     */
    private List<String> saleModelImages;
    /**
     * 总价格
     */
    private BigDecimal totalPrice;
    /**
     * 是否有效
     */
    private Boolean isValid;
}
