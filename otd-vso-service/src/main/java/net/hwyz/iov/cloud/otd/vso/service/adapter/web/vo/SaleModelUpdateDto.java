package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型更新请求 DTO。
 */
@Data
public class SaleModelUpdateDto {

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
}
