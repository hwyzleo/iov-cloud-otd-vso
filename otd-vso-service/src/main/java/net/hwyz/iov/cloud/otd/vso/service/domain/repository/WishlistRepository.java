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

    boolean existsByUserIdAndBuildConfigCode(String userId, String buildConfigCode);

    boolean existsByUserIdAndBuildConfigCodeExcluding(String userId, String buildConfigCode, String excludeWishlistId);

}