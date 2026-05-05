package net.hwyz.iov.cloud.otd.vso.api.enums;

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
    /** 外观 **/
    EXTERIOR,
    /** 内饰 **/
    INTERIOR,
    /** 车轮 **/
    WHEEL,
    /** 轮胎 **/
    TIRE,
    /** 备胎 **/
    SPARE_TIRE,
    /** 智驾 **/
    ADAS,
    /** 座椅 **/
    SEAT;
    public static SaleModelConfigType valOf(String val) {
        return Arrays.stream(SaleModelConfigType.values())
                .filter(saleModelConfigType -> saleModelConfigType.name().equals(val))
                .findFirst()
                .orElse(null);
    }

}
