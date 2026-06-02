package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 管理后台销售车型视图对象。
 */
@Data
public class SaleModelVo {

    private Long id;

    private String saleModelCode;

    private String carlineCode;

    private String modelName;

    private String parameters;

    private List<String> images;

    private Boolean earnestMoney;

    private Boolean downPayment;

    private Boolean enable;

    @JsonProperty("sortWeight")
    private Integer sort;

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
