package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型配置特征族视图对象（包含特征族信息和特征值列表）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigFamilyVo {

    /**
     * 特征族ID
     */
    private Long familyId;

    /**
     * 特征族编码
     */
    private String familyCode;

    /**
     * 特征族名称
     */
    private String familyName;

    /**
     * 特征族价格（可选）
     */
    private BigDecimal familyPrice;

    /**
     * 特征族图片
     */
    private List<String> familyImage;

    /**
     * 特征族描述
     */
    private String familyDesc;

    /**
     * 特征族参数
     */
    private String familyParam;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 特征值列表
     */
    private List<SaleModelConfigVo> features;

}