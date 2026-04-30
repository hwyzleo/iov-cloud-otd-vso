package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

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

    private String saleCode;

    private String modelName;

    private List<String> images;

    private Boolean earnestMoney;

    private BigDecimal earnestMoneyPrice;

    private Boolean downPayment;

    private BigDecimal downPaymentPrice;

    private Boolean enable;

    private Integer sort;

    private Instant createTime;

    private String createBy;

    private Instant modifyTime;

    private String modifyBy;
}
