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
    private Map<String, String> saleModelConfigType;
    private Map<String, String> saleModelConfigName;
    private Map<String, BigDecimal> saleModelConfigPrice;
    private BigDecimal totalPrice;
    private List<String> saleModelImages;
    private String saleModelDesc;

}
