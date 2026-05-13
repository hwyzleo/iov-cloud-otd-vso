package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理后台销售车型配置
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigMpt implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 销售车型代码
     */
    private String saleModelCode;

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

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer sort;

}
