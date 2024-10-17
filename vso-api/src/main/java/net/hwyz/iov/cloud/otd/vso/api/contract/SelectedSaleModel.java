package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.*;
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
     * 车型图片集
     */
    private List<String> modelImages;

    /**
     * 车型描述
     */
    private String modelDesc;

    /**
     * 车型配置名称
     */
    private Map<String, String> modelConfigName;

    /**
     * 车型配置价格
     */
    private Map<String, BigDecimal> modelConfigPrice;

    /**
     * 总价格
     */
    private BigDecimal totalPrice;

    /**
     * 购车权益简介
     */
    private String purchaseBenefitsIntro;

}
