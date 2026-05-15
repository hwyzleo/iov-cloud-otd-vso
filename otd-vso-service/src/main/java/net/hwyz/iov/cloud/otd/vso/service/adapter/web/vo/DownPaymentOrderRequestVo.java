package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownPaymentOrderRequestVo {

    private String saleModelCode;
    private String orderNo;
    private Map<String, String> saleModelConfigType;
    private String customerType;
    private String paymentMethod;
    private String licenseCityCode;
    private Integer orderPersonType;
    private Integer purchasePlan;
    private String orderPersonName;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private String dealership;
    private String deliveryCenter;

}
