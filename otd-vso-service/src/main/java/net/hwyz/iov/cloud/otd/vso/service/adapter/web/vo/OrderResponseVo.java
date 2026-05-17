package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    private Map<String, String> saleModelConfigType;
    private Map<String, String> saleModelConfigName;
    private Map<String, BigDecimal> saleModelConfigPrice;
    private BigDecimal totalPrice;
    private List<String> saleModelImages;
    private String saleModelDesc;

}
