package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 心愿单聚合根
 *
 * @author VSO Team
 */
@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {

    private String id;
    private String userId;
    private String saleModel;
    private String buildConfigCode;
    private String wishlistName;
    private WishlistStatus status;
    private Date createTime;
    private Date modifyTime;

    public static Wishlist create(String userId, String saleModel, String buildConfigCode) {
        Wishlist wishlist = new Wishlist();
        wishlist.id = IdUtil.nanoId(15);
        wishlist.userId = userId;
        wishlist.saleModel = saleModel;
        wishlist.buildConfigCode = buildConfigCode;
        wishlist.status = WishlistStatus.ACTIVE;
        wishlist.createTime = new Date();
        wishlist.modifyTime = new Date();
        return wishlist;
    }

    public void modify(String buildConfigCode) {
        this.buildConfigCode = buildConfigCode;
        this.modifyTime = new Date();
    }

    public void delete() {
        this.status = WishlistStatus.DELETED;
        this.modifyTime = new Date();
    }

}