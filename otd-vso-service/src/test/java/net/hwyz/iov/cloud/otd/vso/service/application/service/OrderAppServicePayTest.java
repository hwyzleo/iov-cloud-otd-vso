package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PayCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.PayResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.model.Wishlist;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServicePayTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void testEarnestMoneyOrderDeletesWishlist() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleCode = "TEST_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleCode, buildConfigCode);
        wishlistRepository.save(wishlist);
        
        EarnestMoneyCmd createCmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .regionCode("TEST_REGION")
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(createCmd);
        String orderNo = result.getOrderNo();
        
        assertNotNull(result, "下单结果应该不为空");
        assertNotNull(orderNo, "订单号应该不为空");
        
        Wishlist wishlistAfterOrder = wishlistRepository.findByUserId(userId).stream().findFirst().orElse(null);
        assertNull(wishlistAfterOrder, "下单成功后心愿单应该被删除");
    }
}