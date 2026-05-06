package net.hwyz.iov.cloud.otd.vso.api.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

/**
 * 销售车型配置类型枚举类
 * 
 * @deprecated 已被动态特征值模式替代，销售车型配置现在从生产配置的特征值动态生成，
 *             不再使用固定的配置类型枚举。请使用 {@code Map<String, String>} 
 *             来表示特征族编码和特征值编码的映射关系。
 *             该枚举将在下一版本中移除。
 *
 * @author hwyz_leo
 */
@Deprecated(since = "2026-05", forRemoval = true)
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
