package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.WishlistNotExistException;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceEarnestMoneyTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void testEarnestMoneyOrderWithWishlistId() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleCode = "TEST_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleCode, buildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        String smallOrderNo = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(smallOrderNo, "小订单号应该不为空");
        
        Wishlist savedWishlist = wishlistRepository.findByWishlistId(wishlist.getId()).orElse(null);
        assertNotNull(savedWishlist, "心愿单在支付成功前应该还存在");
    }

    @Test
    void testEarnestMoneyOrderWithNonExistentWishlist() {
        String userId = "test_user_" + System.currentTimeMillis();
        String nonExistentWishlistId = "NON_EXISTENT_ID";
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(nonExistentWishlistId)
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        assertThrows(WishlistNotExistException.class, () -> {
            orderAppService.earnestMoneyOrder(cmd);
        }, "应该抛出心愿单不存在异常");
    }

    @Test
    void testEarnestMoneyOrderWithWishlistOverrideSaleCode() {
        String userId = "test_user_" + System.currentTimeMillis();
        String wishlistSaleCode = "WISHLIST_SALE_CODE";
        String overrideSaleCode = "OVERRIDE_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, wishlistSaleCode, buildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .saleCode(overrideSaleCode)
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        String smallOrderNo = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(smallOrderNo, "小订单号应该不为空");
    }

    @Test
    void testEarnestMoneyOrderWithWishlistOverrideBuildConfigCode() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleCode = "TEST_SALE_CODE";
        String wishlistBuildConfigCode = "WISHLIST_BUILD_CONFIG";
        String overrideBuildConfigCode = "OVERRIDE_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleCode, wishlistBuildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .buildConfigCode(overrideBuildConfigCode)
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        String smallOrderNo = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(smallOrderNo, "小订单号应该不为空");
    }
}