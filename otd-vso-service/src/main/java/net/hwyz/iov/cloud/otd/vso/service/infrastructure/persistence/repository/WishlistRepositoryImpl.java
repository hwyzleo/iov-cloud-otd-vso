package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter.WishlistPoConverter;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.mapper.WishlistMapper;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 心愿单仓储实现
 *
 * @author VSO Team
 */
@Repository
@RequiredArgsConstructor
public class WishlistRepositoryImpl implements WishlistRepository {

    private final WishlistMapper wishlistMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Wishlist save(Wishlist wishlist) {
        WishlistPo existingPo = wishlistMapper.selectByWishlistId(wishlist.getId());
        WishlistPo po = WishlistPoConverter.INSTANCE.toPo(wishlist);
        
        if (existingPo != null) {
            po.setId(existingPo.getId());
            wishlistMapper.updatePo(po);
        } else {
            wishlistMapper.insertPo(po);
        }
        return WishlistPoConverter.INSTANCE.toDomain(po);
    }

    @Override
    public Optional<Wishlist> findByWishlistId(String wishlistId) {
        return Optional.ofNullable(wishlistMapper.selectByWishlistId(wishlistId))
                .map(WishlistPoConverter.INSTANCE::toDomain);
    }

    @Override
    public Optional<Wishlist> findByWishlistIdAndUserId(String wishlistId, String userId) {
        return Optional.ofNullable(wishlistMapper.selectByWishlistIdAndUserId(wishlistId, userId))
                .map(WishlistPoConverter.INSTANCE::toDomain);
    }

    @Override
    public List<Wishlist> findByUserId(String userId) {
        return WishlistPoConverter.INSTANCE.toDomainList(wishlistMapper.selectByUserId(userId));
    }

    @Override
    public void deleteByUserId(String userId) {
        wishlistMapper.deleteByUserId(userId);
    }

}