package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Map;

/**
 * 心愿单
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 销售车型配置类型
     * key: 销售车型配置类型
     * value: 销售车型配置类型代码
     */
    Map<String, String> saleModelConfigType;

}
