package net.hwyz.iov.cloud.otd.vso.service.domain.model;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    private String id;
    private String userId;
    private String saleModelCode;
    private String configurationCode;
    private List<String> optionCodes;
    private String optionCodesHash;
    private String wishlistName;
    private WishlistStatus status;
    private String invalidReason;
    private Date createTime;
    private Date modifyTime;

    /**
     * 创建心愿单
     */
    public static Wishlist create(String userId, String saleModelCode, String configurationCode, List<String> optionCodes) {
        Wishlist wishlist = new Wishlist();
        wishlist.id = IdUtil.nanoId(15);
        wishlist.userId = userId;
        wishlist.saleModelCode = saleModelCode;
        wishlist.configurationCode = configurationCode;
        wishlist.optionCodes = optionCodes != null ? new ArrayList<>(optionCodes) : new ArrayList<>();
        wishlist.optionCodesHash = calculateOptionCodesHash(wishlist.optionCodes);
        wishlist.status = WishlistStatus.ACTIVE;
        wishlist.createTime = new Date();
        wishlist.modifyTime = new Date();
        return wishlist;
    }

    /**
     * 修改心愿单
     */
    public void modify(String configurationCode, List<String> optionCodes) {
        this.configurationCode = configurationCode;
        this.optionCodes = optionCodes != null ? new ArrayList<>(optionCodes) : new ArrayList<>();
        this.optionCodesHash = calculateOptionCodesHash(this.optionCodes);
        this.invalidReason = null;
        this.modifyTime = new Date();
    }

    /**
     * 删除心愿单
     */
    public void delete() {
        this.status = WishlistStatus.DELETED;
        this.modifyTime = new Date();
    }

    /**
     * 标记心愿单失效
     */
    public void markInvalid(String reason) {
        this.invalidReason = reason;
        this.modifyTime = new Date();
    }

    /**
     * 计算 optionCodes 排序后的哈希值
     */
    private static String calculateOptionCodesHash(List<String> optionCodes) {
        if (optionCodes == null || optionCodes.isEmpty()) {
            return "";
        }
        List<String> sorted = new ArrayList<>(optionCodes);
        Collections.sort(sorted);
        String joined = String.join(",", sorted);
        return DigestUtil.md5Hex(joined);
    }
}