package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 特征值范围 Vo。
 */
@Data
public class FeatureCodeRangeVo {

    private String familyCode;

    private String familyName;

    private String[] featureCode;

    private String[] featureName;
}