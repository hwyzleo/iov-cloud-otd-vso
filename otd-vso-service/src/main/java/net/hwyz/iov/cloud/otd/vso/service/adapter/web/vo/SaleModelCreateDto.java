package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售车型创建请求 DTO。
 */
@Data
public class SaleModelCreateDto {

    @NotBlank(message = "销售车型代码不能为空")
    private String saleModelCode;

    /**
     * MDM Carline 编码（1:1 绑定）
     */
    @NotBlank(message = "车系代码不能为空")
    private String carlineCode;

    @NotBlank(message = "车型名称不能为空")
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

    private Boolean earnestMoney;

    private Boolean downPayment;

    /**
     * 上架状态：active/off_shelf
     */
    private String listingStatus;

    /**
     * 上架生效开始时间
     */
    @NotNull(message = "上架生效开始时间不能为空")
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
