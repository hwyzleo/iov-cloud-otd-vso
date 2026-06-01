package net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * 创建/更新 Variant 销售策略命令
 *
 * @author hwyz_leo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariantPolicyCmd {

    private String saleModelCode;
    private String variantCode;
    private String saleStatus;
    private BigDecimal variantPrice;
    private BigDecimal earnestMoneyPrice;
    private BigDecimal downPaymentPrice;
    private List<String> availableRegions;
    private List<String> channels;
    private String marketingName;
    private String marketingImage;
    private String marketingCopy;
    private Integer sortWeight;
    private Timestamp effectiveFrom;
    private Timestamp effectiveTo;
}
