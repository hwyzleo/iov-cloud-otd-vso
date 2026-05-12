package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.DownPaymentCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.PayCmd;
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
class OrderAppServiceDownPaymentTest {

    @Autowired
    private OrderAppService orderAppService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Test
    void testDownPaymentOrderWithWishlistId() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleCode = "TEST_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        Wishlist wishlist = Wishlist.create(userId, saleCode, buildConfigCode);
        wishlistRepository.save(wishlist);
        
        DownPaymentCmd cmd = DownPaymentCmd.builder()
            .accountId(userId)
            .wishlistId(wishlist.getId())
            .buildConfigCode(buildConfigCode)
            .licenseCityCode("TEST_CITY")
            .build();
        
        String orderNo = orderAppService.downPaymentOrder(cmd);
        
        assertNotNull(orderNo, "订单号应该不为空");
        
        Wishlist wishlistBeforePay = wishlistRepository.findByUserId(userId).stream().findFirst().orElse(null);
        assertNotNull(wishlistBeforePay, "支付前心愿单应该存在");
        
        PayCmd payCmd = PayCmd.builder()
            .accountId(userId)
            .orderNo(orderNo)
            .paymentAmount(new BigDecimal("5000"))
            .build();
        
        PayResult payResult = orderAppService.pay(payCmd);
        
        assertNotNull(payResult, "支付结果应该不为空");
        
        Wishlist wishlistAfterPay = wishlistRepository.findByUserId(userId).stream().findFirst().orElse(null);
        assertNull(wishlistAfterPay, "支付定金成功后心愿单应该被删除");
    }
}