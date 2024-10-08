package net.hwyz.iov.cloud.otd.vso.api.contract.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车型类型枚举类
 *
 * @author hwyz_leo
 */
@AllArgsConstructor
public enum SaleModelType {

    /** 车型 **/
    MODEL,
    /** 备胎 **/
    SPIRE_TIRE,
    /** 外观 **/
    EXTERIOR,
    /** 车轮 **/
    WHEEL,
    /** 内饰 **/
    INTERIOR,
    /** 选装 **/
    OPTIONAL;
    public static SaleModelType valOf(String val) {
        return Arrays.stream(SaleModelType.values())
                .filter(saleModelType -> saleModelType.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
