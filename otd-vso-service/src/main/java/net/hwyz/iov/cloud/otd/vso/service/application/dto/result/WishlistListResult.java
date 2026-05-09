package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 心愿单列表结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class WishlistListResult {

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