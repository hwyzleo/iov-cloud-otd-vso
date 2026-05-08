package net.hwyz.iov.cloud.otd.vso.service.adapter.web.vo;

import lombok.*;

import java.util.Date;

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

}