package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 心愿单详情结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class WishlistDetailResult {

    private String wishlistId;
    private String saleCode;
    private String buildConfigCode;
    private Date createTime;
    private Date modifyTime;

    private String displayName;
    private List<SaleModelConfigItemResult> saleModelConfigs;
    private List<String> saleModelImages;
    private String saleModelDesc;
    private BigDecimal totalPrice;
    private Boolean isValid;

}
