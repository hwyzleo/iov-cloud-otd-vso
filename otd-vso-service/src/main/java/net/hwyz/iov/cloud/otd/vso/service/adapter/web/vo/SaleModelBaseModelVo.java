package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 销售车型基础车型关联 Vo。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleModelBaseModelVo {

    private Long id;

    private String saleCode;

    private String baseModelCode;

    private String baseModelName;

    private List<String> baseModelImage;

    private BigDecimal baseModelPrice;

    private String baseModelDesc;

    private String baseModelParam;

    private Boolean enable;

    private Integer sort;

    private Instant createTime;

    private String createBy;

    private Instant modifyTime;

    private String modifyBy;
}