package net.hwyz.iov.cloud.otd.vso.api.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车型配置类型枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum SaleModelConfigType {

    /** 车型 **/
    MODEL,
    /** 备胎 **/
    SPARE_TIRE,
    /** 外观 **/
    EXTERIOR,
    /** 车轮 **/
    WHEEL,
    /** 内饰 **/
    INTERIOR,
    /** 选装 **/
    OPTIONAL;
    public static SaleModelConfigType valOf(String val) {
        return Arrays.stream(SaleModelConfigType.values())
                .filter(saleModelConfigType -> saleModelConfigType.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
