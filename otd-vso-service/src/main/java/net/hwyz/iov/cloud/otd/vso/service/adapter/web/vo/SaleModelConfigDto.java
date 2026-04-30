package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型配置 DTO。
 */
@Data
public class SaleModelConfigDto {

    private Long id;

    private String type;

    private String typeCode;

    private String typeName;

    private BigDecimal typePrice;

    private List<String> typeImage;

    private String typeDesc;

    private String typeParam;

    private Boolean enable;

    private Integer sort;
}
