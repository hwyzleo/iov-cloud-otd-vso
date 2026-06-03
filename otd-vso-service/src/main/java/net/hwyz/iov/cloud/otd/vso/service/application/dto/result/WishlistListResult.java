package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 心愿单列表结果
 *
 * @author VSO Team
 */
@Data
@Builder
public class WishlistListResult {

    private String wishlistId;
    private String saleModelCode;
    private String modelCode;
    private String variantCode;
    private String configurationCode;
    private List<String> optionCodes;
    private Date createTime;
    private Date modifyTime;
    private String invalidReason;

}
