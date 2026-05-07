package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponseVo {

    private String saleCode;
    private String orderNo;
    private String saleModelConfigType;
    private String saleModelConfigName;
    private BigDecimal saleModelConfigPrice;
    private BigDecimal totalPrice;
    private List<String> saleModelImages;
    private String saleModelDesc;
    private Boolean isValid;

}
