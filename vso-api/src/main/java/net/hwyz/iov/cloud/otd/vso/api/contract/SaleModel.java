package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 销售车型
 *
 * @author hwyz_leo
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModel {

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 销售车型名称
     */
    private String modelName;

    /**
     * 是否允许意向金（小定）
     */
    private Boolean earnestMoney;

    /**
     * 是否允许定金（大定）
     */
    private Boolean downPayment;

}
