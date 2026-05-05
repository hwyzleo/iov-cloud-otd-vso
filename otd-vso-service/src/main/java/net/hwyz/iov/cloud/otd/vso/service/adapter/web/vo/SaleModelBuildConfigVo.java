package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

/**
 * 销售车型生产配置关联 Vo。
 */
@Data
public class SaleModelBuildConfigVo {

    private Long id;

    private String saleCode;

    private String buildConfigCode;

    private String buildConfigName;

    private Boolean enable;

    private Integer sort;
}