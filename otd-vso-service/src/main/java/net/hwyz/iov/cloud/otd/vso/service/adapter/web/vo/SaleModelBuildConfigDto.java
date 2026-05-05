package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 销售车型生产配置关联 DTO。
 */
@Data
public class SaleModelBuildConfigDto {

    private Long id;

    private String saleCode;

    private String buildConfigCode;

    private Boolean enable;

    private Integer sort;
}