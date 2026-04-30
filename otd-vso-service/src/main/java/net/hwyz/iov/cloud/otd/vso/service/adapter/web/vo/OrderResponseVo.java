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

    private String orderNum;
    private Integer orderState;
    private Date orderTime;
    private Integer orderPersonType;
    private String orderPersonName;
    private Integer orderPersonIdType;
    private String orderPersonIdNum;
    private String licenseCityCode;
    private String licenseCityName;
    private String dealershipCode;
    private String dealershipName;
    private String deliveryCenterCode;
    private String deliveryCenterName;
    private String saleModelConfigType;
    private String saleModelConfigName;
    private BigDecimal saleModelConfigPrice;
    private BigDecimal totalPrice;
    private List<String> saleModelImages;
    private String saleModelDesc;

}
