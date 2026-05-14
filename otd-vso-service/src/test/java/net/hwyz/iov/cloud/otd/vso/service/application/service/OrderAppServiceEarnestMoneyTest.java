package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
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
        String saleModel = "TEST_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleModel, buildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
        
        Wishlist savedWishlist = wishlistRepository.findByWishlistId(wishlist.getId()).orElse(null);
        assertNull(savedWishlist, "心愿单在下单成功后应该被删除");
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
            .saleModel(overrideSaleCode)
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }

    @Test
    void testEarnestMoneyOrderWithWishlistOverrideBuildConfigCode() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleModel = "TEST_SALE_CODE";
        String wishlistBuildConfigCode = "WISHLIST_BUILD_CONFIG";
        String overrideBuildConfigCode = "OVERRIDE_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleModel, wishlistBuildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .buildConfigCode(overrideBuildConfigCode)
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }

    @Test
    void testEarnestMoneyOrderDirectOrder() {
        String userId = "test_user_direct_" + System.currentTimeMillis();
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel("DIRECT_SALE_CODE")
            .buildConfigCode("DIRECT_BUILD_CONFIG")
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }
}