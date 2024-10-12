package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 订单
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * 订单号
     */
    private String orderNum;

    /**
     * 订单状态
     */
    private Integer orderState;

    /**
     * 显示名称
     */
    private String displayName;

}
