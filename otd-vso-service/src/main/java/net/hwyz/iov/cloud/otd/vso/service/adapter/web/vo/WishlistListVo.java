package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 心愿单列表 Vo
 *
 * @author VSO Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistListVo {

    private String wishlistId;
    private String saleCode;
    private String buildConfigCode;
    private Date createTime;
    private Date modifyTime;

    private Map<String, String> saleModelConfigType;
    private Map<String, String> saleModelConfigName;
    private List<String> saleModelImages;
    private BigDecimal totalPrice;
    private Boolean isValid;

}