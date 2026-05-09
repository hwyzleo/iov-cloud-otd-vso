package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;

/**
 * 销售车型配置项 Vo
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigItemVo {

    /**
     * 特征族代码（如 BASE_MODEL、QA、NA）
     */
    private String familyCode;

    /**
     * 特征族名称（如 "车型"、"外饰颜色"、"内饰风格"）
     */
    private String familyName;

    /**
     * 特征值代码（如 XREHS5LAAA、QA01、NA01）
     */
    private String featureCode;

    /**
     * 特征值名称（如 "标准版26款"、"星夜黑"、"墨玉黑"）
     */
    private String featureName;

    /**
     * 特征值价格
     */
    private BigDecimal featurePrice;

    /**
     * 特征值图片列表
     */
    private java.util.List<String> featureImages;

}