package net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 心愿单 DTO Assembler
 *
 * @author VSO Team
 */
@Mapper
public interface WishlistDtoAssembler {

    WishlistDtoAssembler INSTANCE = Mappers.getMapper(WishlistDtoAssembler.class);

    WishlistListResult toWishlistListResult(Wishlist wishlist);

    List<WishlistListResult> toWishlistListResultList(List<Wishlist> wishlists);

    WishlistDetailResult toWishlistDetailResult(Wishlist wishlist);

}