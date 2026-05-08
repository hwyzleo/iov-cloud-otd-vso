package net.hwyz.iov.cloud.otd.vso.service.application.dto.result;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

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

}