package net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.converter;

import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.persistence.po.WishlistPo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 心愿单数据对象转换类
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistPoConverter {

    WishlistPoConverter INSTANCE = Mappers.getMapper(WishlistPoConverter.class);

    @Mapping(target = "id", source = "wishlistId")
    Wishlist toDomain(WishlistPo po);

    @Mapping(target = "wishlistId", source = "id")
    @Mapping(target = "id", ignore = true)
    WishlistPo toPo(Wishlist domain);

    List<Wishlist> toDomainList(List<WishlistPo> poList);

}