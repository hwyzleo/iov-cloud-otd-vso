package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVo {

    private String orderNo;
    private Integer orderState;
    private String displayName;
    
    private String customerType;
    private String paymentMethod;
    private Integer orderPersonType;
    private String orderPersonName;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private Integer purchasePlan;
    private String licenseCityCode;
    private String dealership;
    private String deliveryCenter;

}
