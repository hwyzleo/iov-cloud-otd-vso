package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型配置项结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class SaleModelConfigItemResult {

    private String familyCode;
    private String familyName;
    private String featureCode;
    private String featureName;
    private BigDecimal featurePrice;
    private List<String> featureImages;

}