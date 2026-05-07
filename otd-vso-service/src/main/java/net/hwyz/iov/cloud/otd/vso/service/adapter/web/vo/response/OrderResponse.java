package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.response;

import lombok.*;

/**
 * M端订单响应
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderNo;
    private Integer orderState;
    private java.util.Date orderTime;
    private Integer orderPersonType;
    private String orderPersonName;
    private String licenseCityCode;
    private String dealershipCode;
    private String deliveryCenterCode;
    private String saleModelConfigType;
    private String saleModelConfigName;
    private java.math.BigDecimal saleModelConfigPrice;
    private java.util.List<String> saleModelImages;
    private String saleModelDesc;
    private java.math.BigDecimal totalPrice;

}
