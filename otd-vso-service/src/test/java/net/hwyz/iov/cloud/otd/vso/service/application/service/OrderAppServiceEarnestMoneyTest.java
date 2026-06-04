package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.EarnestMoneyCmd;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.result.EarnestMoneyOrderResult;
import net.hwyz.iov.cloud.otd.vso.service.domain.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

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
        String modelCode = "TEST_MODEL";
        String variantCode = "TEST_VARIANT";
        List<String> optionCodes = Arrays.asList("OPTION_1", "OPTION_2");
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel(saleModel)
            .modelCode(modelCode)
            .variantCode(variantCode)
            .optionCodes(optionCodes)
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }

    @Test
    void testEarnestMoneyOrderDirectOrder() {
        String userId = "test_user_direct_" + System.currentTimeMillis();
        List<String> optionCodes = Arrays.asList("DIRECT_OPTION_1");
        
        EarnestMoneyCmd cmd = EarnestMoneyCmd.builder()
            .accountId(userId)
            .saleModel("DIRECT_SALE_CODE")
            .modelCode("DIRECT_MODEL")
            .variantCode("DIRECT_VARIANT")
            .optionCodes(optionCodes)
            .licenseCityCode("TEST_CITY")
            .build();
        
        EarnestMoneyOrderResult result = orderAppService.earnestMoneyOrder(cmd);
        
        assertNotNull(result, "返回结果应该不为空");
        assertNotNull(result.getOrderNo(), "订单号应该不为空");
    }
}