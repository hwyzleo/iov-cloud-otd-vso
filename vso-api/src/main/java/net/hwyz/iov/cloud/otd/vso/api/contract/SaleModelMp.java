package net.hwyz.iov.cloud.otd.vso.api.contract;

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
     * 销售代码
     */
    private String saleCode;

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

}
