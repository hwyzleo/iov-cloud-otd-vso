package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 销售车型结果 DTO
 *
 * @author VSO Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelResult {

    private Long id;
    private String saleModelCode;
    private String modelName;
    private String variantCode;
    private String parameters;
    private List<String> images;
    private Boolean earnestMoney;
    private BigDecimal earnestMoneyPrice;
    private Boolean downPayment;
    private BigDecimal downPaymentPrice;
    private Boolean enable;
    private Integer sort;
    private BigDecimal basePrice;
    private String icon;
    private String marketingCopy;
    private String listingStatus;
    private Instant effectiveFrom;
    private Instant effectiveTo;
    private List<String> availableRegions;
    private List<String> channels;
    private Instant createTime;
    private String createBy;
    private Instant modifyTime;
    private String modifyBy;

}
