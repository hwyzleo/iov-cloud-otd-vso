package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 特征值详细信息 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureCodeDetailVo {

    /**
     * 特征值编码
     */
    private String featureCode;

    /**
     * 特征值名称（完整名称，包含特征族名称）
     */
    private String featureName;

    /**
     * 特征值价格
     */
    private BigDecimal featurePrice;

    /**
     * 特征值图片列表
     */
    private List<String> featureImage;

    /**
     * 特征值描述
     */
    private String featureDesc;

    /**
     * 特征值参数
     */
    private String featureParam;

    /**
     * 是否启用
     */
    private Boolean enable;

    /**
     * 排序
     */
    private Integer sort;

}