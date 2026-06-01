package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手机销售车型
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelMp {

    /**
     * 销售车型代码
     */
    private String saleModelCode;

    /**
     * Carline 编码
     */
    private String carlineCode;

    /**
     * Carline 名称
     */
    private String carlineName;

    /**
     * 销售车型名称
     */
    private String modelName;

    /**
     * 起售价（派生字段，取该 SaleModel 下当前可售 Variant 的 min(variantPrice)）
     */
    private BigDecimal startingPrice;

    /**
     * 意向金价格（派生字段，取该 SaleModel 下当前可售 Variant 的 min(earnestMoneyPrice)）
     */
    private BigDecimal earnestMoneyPrice;

    /**
     * 销售车型图片集
     */
    private List<String> images;

    /**
     * 卖点文案
     */
    private String marketingCopy;

    /**
     * 车型图标
     */
    private String icon;

    /**
     * 排序权重
     */
    private Integer sortWeight;

}
