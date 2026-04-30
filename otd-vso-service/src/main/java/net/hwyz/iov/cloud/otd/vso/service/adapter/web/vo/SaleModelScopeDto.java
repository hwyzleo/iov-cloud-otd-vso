package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.util.List;

/**
 * 销售车型可售范围 DTO。
 */
@Data
public class SaleModelScopeDto {

    private Long id;

    private String saleCode;

    private String regionCode;

    private String regionName;

    private List<String> storeCodes;

    private Boolean enable;

    private Integer sort;
}
