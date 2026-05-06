package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 特征值范围 Vo（包含完整信息）
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCodeRangeVo {

    /**
     * 特征族编码
     */
    private String familyCode;

    /**
     * 特征族名称
     */
    private String familyName;

    /**
     * 特征族价格
     */
    private BigDecimal familyPrice;

    /**
     * 特征族图片列表
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
     * 特征值详细信息列表（包含价格、图片、描述等完整信息）
     */
    private List<FeatureCodeDetailVo> featureDetails;

}