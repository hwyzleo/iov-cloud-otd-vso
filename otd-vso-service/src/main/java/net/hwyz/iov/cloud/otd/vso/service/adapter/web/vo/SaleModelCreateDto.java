package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售车型创建请求 DTO。
 */
@Data
public class SaleModelCreateDto {

    private String saleCode;

    private String modelName;

    private String parameters;

    private List<String> images;

    private Boolean earnestMoney;

    private BigDecimal earnestMoneyPrice;

    private Boolean downPayment;

    private BigDecimal downPaymentPrice;

    private Boolean enable;

    private Integer sort;
}
