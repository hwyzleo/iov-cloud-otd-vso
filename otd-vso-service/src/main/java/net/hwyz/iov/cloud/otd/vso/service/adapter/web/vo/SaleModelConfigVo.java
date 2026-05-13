package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 管理后台销售车型配置视图对象。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelConfigVo {

    private Long id;

    private String saleModelCode;

    private String type;

    private String typeCode;

    private String typeName;

    private BigDecimal typePrice;

    private List<String> typeImage;

    private String typeDesc;

    private String typeParam;

    private Boolean enable;

    private Integer sort;

    private Instant createTime;

    private String createBy;

    private Instant modifyTime;

    private String modifyBy;
}
