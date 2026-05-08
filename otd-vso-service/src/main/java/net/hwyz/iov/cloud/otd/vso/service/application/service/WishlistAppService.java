package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.service.adapter.web.assembler.WishlistDtoAssembler;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.CreateWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DeleteWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyWishlistCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistDetailResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.WishlistListResult;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 心愿单应用服务
 *
 * @author VSO Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistAppService {

    private final WishlistRepository wishlistRepository;

    @Transactional(rollbackFor = Exception.class)
    public String createWishlist(CreateWishlistCmd cmd) {
        log.info("创建心愿单：accountId={}, saleCode={}", cmd.getAccountId(), cmd.getSaleCode());
        Wishlist wishlist = Wishlist.create(cmd.getAccountId(), cmd.getSaleCode(), cmd.getBuildConfigCode());
        wishlistRepository.save(wishlist);
        log.info("心愿单创建成功：wishlistId={}", wishlist.getId());
        return wishlist.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void modifyWishlist(ModifyWishlistCmd cmd) {
        log.info("修改心愿单：wishlistId={}, accountId={}", cmd.getWishlistId(), cmd.getAccountId());
        Wishlist wishlist = findWishlistById(cmd.getAccountId(), cmd.getWishlistId());
        wishlist.modify(cmd.getBuildConfigCode());
        wishlistRepository.save(wishlist);
        log.info("心愿单修改成功：wishlistId={}", wishlist.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteWishlist(DeleteWishlistCmd cmd) {
        log.info("删除心愿单：wishlistId={}, accountId={}", cmd.getWishlistId(), cmd.getAccountId());
        Wishlist wishlist = findWishlistById(cmd.getAccountId(), cmd.getWishlistId());
        wishlist.delete();
        wishlistRepository.save(wishlist);
        log.info("心愿单删除成功：wishlistId={}", wishlist.getId());
    }

    public List<WishlistListResult> getWishlistList(String accountId) {
        log.info("获取心愿单列表：accountId={}", accountId);
        List<Wishlist> wishlists = wishlistRepository.findByUserId(accountId);
        return wishlists.stream()
                .map(WishlistDtoAssembler.INSTANCE::toWishlistListResult)
                .collect(Collectors.toList());
    }

    public WishlistDetailResult getWishlistDetail(String accountId, String wishlistId) {
        log.info("获取心愿单详情：wishlistId={}, accountId={}", wishlistId, accountId);
        Wishlist wishlist = findWishlistById(accountId, wishlistId);
        return WishlistDtoAssembler.INSTANCE.toWishlistDetailResult(wishlist);
    }

    private Wishlist findWishlistById(String accountId, String wishlistId) {
        return wishlistRepository.findByWishlistIdAndUserId(wishlistId, accountId)
                .orElseThrow(() -> new WishlistNotExistException(wishlistId));
    }

}