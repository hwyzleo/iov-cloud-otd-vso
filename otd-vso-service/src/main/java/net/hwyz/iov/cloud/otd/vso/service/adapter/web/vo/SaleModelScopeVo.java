package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * 销售车型可售范围视图对象。
 */
@Data
public class SaleModelScopeVo {

    private Long id;

    private String saleCode;

    private String regionCode;

    private String regionName;

    private List<String> storeCodes;

    private Boolean enable;

    private Integer sort;

    private Instant createTime;

    private String createBy;

    private Instant modifyTime;

    private String modifyBy;
}
