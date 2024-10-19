package net.hwyz.iov.cloud.otd.vso.api.contract.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.hwyz.iov.cloud.otd.vso.api.contract.SelectedSaleModel;

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
public class WishlistResponse extends SelectedSaleModel {
    /**
     * 订单号
     */
    private String orderNum;
    /**
     * 是否有效
     */
    private Boolean isValid;
}
