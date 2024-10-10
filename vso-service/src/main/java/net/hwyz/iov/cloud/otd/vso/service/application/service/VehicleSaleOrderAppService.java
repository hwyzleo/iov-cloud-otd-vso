package net.hwyz.iov.cloud.otd.vso.service.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.hwyz.iov.cloud.otd.vso.api.contract.Wishlist;
import net.hwyz.iov.cloud.otd.vso.api.contract.response.WishlistResponse;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.exception.SaleModelTypeCodeNoteExistException;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.WishlistDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.dao.WishlistDetailDao;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.SaleModelPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.WishlistDetailPo;
import net.hwyz.iov.cloud.otd.vso.service.infrastructure.repository.po.WishlistPo;
import net.hwyz.iov.cloud.tsp.framework.commons.enums.Symbol;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆销售订单相关应用服务类
 *
 * @author hwyz_leo
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleSaleOrderAppService {

    private final WishlistDao wishlistDao;
    private final WishlistDetailDao wishlistDetailDao;
    private final SaleModelAppService saleModelAppService;

    /**
     * 创建用户心愿单
     *
     * @param accountId 账号ID
     * @param wishlist  心愿单
     */
    public void createUserWishlist(String accountId, Wishlist wishlist) {
        WishlistPo wishlistPo = WishlistPo.builder()
                .accountId(accountId)
                .saleCode(wishlist.getSaleCode())
                .saleModelCode(wishlist.getSaleModelCode())
                .isOrder(false)
                .build();
        wishlistDao.insertPo(wishlistPo);
        batchCreateWishlistDetail(wishlist, wishlistPo.getId());
    }

    /**
     * 修改用户心愿单
     *
     * @param accountId 账号ID
     * @param wishlist  心愿单
     */
    public void modifyUserWishlist(String accountId, Wishlist wishlist) {
        WishlistPo userWishlistPo = getUserWishlistPo(accountId);
        if (userWishlistPo != null) {
            userWishlistPo.setSaleCode(wishlist.getSaleCode());
            userWishlistPo.setSaleModelCode(wishlist.getSaleModelCode());
            wishlistDao.updatePo(userWishlistPo);
            wishlistDetailDao.physicalDeletePoByWishlistId(userWishlistPo.getId());
            batchCreateWishlistDetail(wishlist, userWishlistPo.getId());
        } else {
            logger.warn("用户[{}]心愿单不存在，修改改为新增", accountId);
            createUserWishlist(accountId, wishlist);
        }
    }

    /**
     * 批量新增心愿单详情
     *
     * @param wishlist   心愿单
     * @param wishlistId 心愿单ID
     */
    private void batchCreateWishlistDetail(Wishlist wishlist, Long wishlistId) {
        Map<String, SaleModelPo> saleModelMap = saleModelAppService.getSaleModelMap(wishlist.getSaleCode());
        List<WishlistDetailPo> wishlistDetailPoList = new ArrayList<>();
        wishlist.getSaleModelType().forEach((key, value) -> {
            SaleModelPo saleModelPo = saleModelMap.get(key + Symbol.UNDERSCORE.value + value);
            if (saleModelPo == null) {
                throw new SaleModelTypeCodeNoteExistException(wishlist.getSaleCode(), key, value);
            }
            WishlistDetailPo wishlistDetailPo = WishlistDetailPo.builder()
                    .wishlistId(wishlistId)
                    .saleModelType(key)
                    .saleModelTypeCode(value)
                    .saleName(saleModelPo.getSaleName())
                    .salePrice(saleModelPo.getSalePrice())
                    .build();
            wishlistDetailPoList.add(wishlistDetailPo);
        });
        wishlistDetailDao.batchInsertPo(wishlistDetailPoList);
    }

    /**
     * 删除用户心愿单
     *
     * @param accountId 账号ID
     */
    public void deleteUserWishlist(String accountId) {
        WishlistPo userWishlistPo = getUserWishlistPo(accountId);
        if (userWishlistPo != null) {
            wishlistDetailDao.physicalDeletePoByWishlistId(userWishlistPo.getId());
            wishlistDao.physicalDeletePo(userWishlistPo.getId());
        } else {
            logger.warn("用户[{}]心愿单不存在，忽略删除", accountId);
        }
    }

    /**
     * 获取用户心愿单详情
     *
     * @param accountId 账号ID
     * @return 用户心愿单详情
     */
    public WishlistResponse getUserWishlistResponse(String accountId) {
        WishlistPo wishlistPo = getUserWishlistPo(accountId);
        if (wishlistPo == null) {
            return null;
        }
        Map<String, String> saleModelType = new HashMap<>();
        Map<String, String> saleModelName = new HashMap<>();
        Map<String, BigDecimal> saleModelPrice = new HashMap<>();
        boolean isValid = true;
        BigDecimal totalPrice = BigDecimal.ZERO;
        Map<String, SaleModelPo> saleModelMap = saleModelAppService.getSaleModelMap(wishlistPo.getSaleCode());
        for (WishlistDetailPo wishlistDetailPo : wishlistDetailDao.selectPoByExample(WishlistDetailPo.builder().wishlistId(wishlistPo.getId()).build())) {
            saleModelType.put(wishlistDetailPo.getSaleModelType(), wishlistDetailPo.getSaleModelTypeCode());
            saleModelName.put(wishlistDetailPo.getSaleModelType(), wishlistDetailPo.getSaleName());
            saleModelPrice.put(wishlistDetailPo.getSaleModelType(), wishlistDetailPo.getSalePrice());
            totalPrice = totalPrice.add(wishlistDetailPo.getSalePrice());
            if (isValid) {
                SaleModelPo saleModelPo = saleModelMap.get(wishlistDetailPo.getSaleModelType() + Symbol.UNDERSCORE.value + wishlistDetailPo.getSaleModelTypeCode());
                if (saleModelPo == null) {
                    isValid = false;
                    continue;
                }
                if (!saleModelPo.getSaleName().equals(wishlistDetailPo.getSaleName())) {
                    isValid = false;
                    continue;
                }
                if (saleModelPo.getSalePrice().compareTo(wishlistDetailPo.getSalePrice()) != 0) {
                    isValid = false;
                }
            }
        }
        return WishlistResponse.builder()
                .saleCode(wishlistPo.getSaleCode())
                .saleModelCode(wishlistPo.getSaleModelCode())
                .saleModelType(saleModelType)
                .saleModelName(saleModelName)
                .saleModelPrice(saleModelPrice)
                .totalPrice(totalPrice)
                .isValid(isValid)
                .build();
    }

    /**
     * 获取用户心愿单详情
     *
     * @param accountId 账号ID
     * @return 用户心愿单详情
     */
    private WishlistPo getUserWishlistPo(String accountId) {
        List<WishlistPo> wishlistPoList = wishlistDao.selectPoByExample(WishlistPo.builder().accountId(accountId).isOrder(false).build());
        if (wishlistPoList.isEmpty()) {
            return null;
        }
        if (wishlistPoList.size() > 1) {
            logger.warn("用户[{}]心愿单数量[{}]大于1", accountId, wishlistPoList.size());
        }
        return wishlistPoList.get(0);
    }

}
