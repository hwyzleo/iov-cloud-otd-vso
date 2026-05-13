package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 已选择的销售车型结果 DTO
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectedSaleModelResult {

    /**
     * 销售车型代码
     */
    private String saleModelCode;

    /**
     * 销售车型名称
     */
    private String modelName;

    /**
     * 销售车型图片集
     */
    private List<String> images;

    /**
     * 是否允许意向金
     */
    private Boolean earnestMoney;

    /**
     * 意向金价格
     */
    private BigDecimal earnestMoneyPrice;

    /**
     * 是否允许定金
     */
    private Boolean downPayment;

    /**
     * 定金价格
     */
    private BigDecimal downPaymentPrice;

    /**
     * 生产配置代码
     */
    private String buildConfigCode;

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
