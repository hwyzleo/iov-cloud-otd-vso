package net.hwyz.iov.cloud.otd.vso.api.contract;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型配置
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigMp {

    /**
     * 销售代码
     */
    private String saleCode;

    /**
     * 销售车型配置类型
     */
    private String type;

    /**
     * 销售车型配置类型代码
     */
    private String typeCode;

    /**
     * 销售车型配置类型名称
     */
    private String typeName;

    /**
     * 销售车型配置类型价格
     */
    private BigDecimal typePrice;

    /**
     * 销售车型配置类型图片
     */
    private List<String> typeImage;

    /**
     * 销售车型配置类型描述
     */
    private String typeDesc;

    /**
     * 销售车型配置类型参数
     */
    private String typeParam;

}
