package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 特征值详细信息列表（包含价格、图片、描述等完整信息）
     */
    private List<FeatureCodeDetailVo> featureDetails;

}