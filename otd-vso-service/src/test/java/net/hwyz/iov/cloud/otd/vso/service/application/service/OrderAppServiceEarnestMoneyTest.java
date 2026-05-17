package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
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
    void testEarnestMoneyOrder() {
        String userId = "test_user_" + System.currentTimeMillis();
        String saleModel = "TEST_SALE_CODE";
        String buildConfigCode = "TEST_BUILD_CONFIG";
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel(saleModel)
            .buildConfigCode(buildConfigCode)
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