package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo.response;

import lombok.*;

/**
 * C端愿望单响应
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {

    private String saleModelCode;
    private String orderNo;
    private String saleModelConfigType;
    private String saleModelConfigName;
    private java.math.BigDecimal saleModelConfigPrice;
    private java.math.BigDecimal totalPrice;
    private java.util.List<String> saleModelImages;
    private String saleModelDesc;
    private Boolean isValid;

}
