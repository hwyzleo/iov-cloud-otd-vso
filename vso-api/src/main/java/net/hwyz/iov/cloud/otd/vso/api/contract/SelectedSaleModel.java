package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 已选择的销售车型
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SelectedSaleModel extends SaleModel {

    /**
     * 车型配置代码
     */
    private String modelConfigCode;

    /**
     * 销售车型图片集
     */
    private List<String> saleModelImages;

    /**
     * 销售车型配置名称
     * key: 销售车型配置类型
     * value: 销售车型配置代码
     */
    private Map<String, String> saleModelConfigType;

    /**
     * 销售车型配置名称
     * key: 销售车型配置类型
     * value: 销售车型配置名称
     */
    private Map<String, String> saleModelConfigName;

    /**
     * 销售车型配置价格
     * key: 销售车型配置类型
     * value: 销售车型配置价格
     */
    private Map<String, BigDecimal> saleModelConfigPrice;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 销售车型描述
     */
    private String saleModelDesc;

    /**
     * 购车权益简介
     */
    private String purchaseBenefitsIntro;

}
