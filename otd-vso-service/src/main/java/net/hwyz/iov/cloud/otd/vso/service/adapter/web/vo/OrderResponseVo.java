package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseVo {

    private String orderNo;
    private Integer orderState;
    private Date orderTime;
    private String customerType;
    private String paymentMethod;
    private Integer orderPersonType;
    private String orderPersonName;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private Integer purchasePlan;
    private String licenseCityCode;
    private String licenseCityName;
    private String orderStoreCode;
    private String orderStoreName;
    private String deliveryStoreCode;
    private String deliveryStoreName;
    private String saleModelCode;
    private String modelCode;
    private String modelName;
    private String variantCode;
    private String variantName;
    private String configurationCode;
    private List<String> optionCodes;
    private List<OptionBreakdownItem> optionBreakdown;
    private BigDecimal totalPrice;
    private List<String> saleModelImages;
    private String saleModelDesc;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionBreakdownItem {
        private String optionCode;
        private String optionFamilyCode;
        private String optionFamilyName;
        private String optionName;
        private BigDecimal optionPrice;
    }

}
