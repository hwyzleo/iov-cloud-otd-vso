package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型基础车型关联 DTO。
 */
@Data
public class SaleModelBaseModelDto {

    private Long id;

    private String baseModelCode;

    private String baseModelName;

    private List<String> baseModelImage;

    private BigDecimal baseModelPrice;

    private String baseModelDesc;

    private String baseModelParam;

    private Boolean enable;

    private Integer sort;
}