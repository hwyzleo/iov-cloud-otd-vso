package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售车型更新请求 DTO。
 */
@Data
public class SaleModelUpdateDto {

    private Long id;

    private String saleModelCode;

    /**
     * MDM Variant 编码（1:1 绑定，修改前需校验锁定）
     */
    private String variantCode;

    @JsonProperty("name")
    private String modelName;

    /**
     * 车型图标 URL
     */
    private String icon;

    private List<String> images;

    /**
     * 卖点文案
     */
    private String marketingCopy;

    /**
     * 起售价
     */
    private BigDecimal basePrice;

    private Boolean earnestMoney;

    private BigDecimal earnestMoneyPrice;

    private Boolean downPayment;

    private BigDecimal downPaymentPrice;

    /**
     * 上架状态：active/off_shelf
     */
    private String listingStatus;

    /**
     * 上架生效开始时间
     */
    private LocalDateTime effectiveFrom;

    /**
     * 上架生效结束时间
     */
    private LocalDateTime effectiveTo;

    /**
     * 可售区域列表，为空表示全国
     */
    private List<String> availableRegions;

    /**
     * 可售渠道列表，为空表示全渠道
     */
    private List<String> channels;

    private Boolean enable;

    @JsonProperty("sortWeight")
    private Integer sort;
}
