package net.hwyz.iov.cloud.otd.vso.service.domain.repository;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;

import java.util.List;
import java.util.Optional;

/**
 * 心愿单仓储接口
 *
 * @author VSO Team
 */
public interface WishlistRepository {

    Wishlist save(Wishlist wishlist);

    Optional<Wishlist> findByWishlistId(String wishlistId);

    Optional<Wishlist> findByWishlistIdAndUserId(String wishlistId, String userId);

    List<Wishlist> findByUserId(String userId);

    void deleteByUserId(String userId);

    long countByUserId(String userId);

    /**
     * 检查是否存在相同配置的有效心愿单
     * 唯一键: (userId, saleModelCode, modelCode, variantCode, configurationCode, optionCodesHash)
     */
    boolean existsByUniqueKey(String userId, String saleModelCode, String modelCode,
                              String variantCode, String configurationCode, String optionCodesHash);

    /**
     * 检查是否存在相同配置的有效心愿单（排除指定心愿单）
     */
    boolean existsByUniqueKeyExcluding(String userId, String saleModelCode, String modelCode,
                                       String variantCode, String configurationCode,
                                       String optionCodesHash, String excludeWishlistId);

    /**
     * 检查是否存在指定销售车型的活跃心愿单
     */
    boolean existsActiveBySaleModelCode(String saleModelCode);

}
