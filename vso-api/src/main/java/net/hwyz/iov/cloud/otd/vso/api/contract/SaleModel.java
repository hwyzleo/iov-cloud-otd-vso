package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 销售车型
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModel {

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 销售车型类型
     */
    private String saleModelType;

    /**
     * 销售车型类型代码
     */
    private String saleModelTypeCode;

    /**
     * 销售名称
     */
    private String saleName;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 销售图片
     */
    private String saleImage;

    /**
     * 销售描述
     */
    private String saleDesc;

    /**
     * 销售参数
     */
    private String saleParam;

}
