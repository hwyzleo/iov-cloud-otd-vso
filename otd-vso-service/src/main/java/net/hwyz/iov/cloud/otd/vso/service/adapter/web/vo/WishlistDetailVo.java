package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 心愿单详情 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistDetailVo {

    private String wishlistId;
    private String saleCode;
    private String buildConfigCode;
    private Date createTime;
    private Date modifyTime;

    private String displayName;
    private List<SaleModelConfigItemVo> saleModelConfigs;
    private List<String> saleModelImages;
    private String saleModelDesc;
    private BigDecimal totalPrice;
    private Boolean isValid;

}