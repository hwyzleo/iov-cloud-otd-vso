package net.hwyz.iov.cloud.otd.vso.service.application.service;

import net.hwyz.iov.cloud.otd.vso.service.BaseTest;
import net.hwyz.iov.cloud.otd.vso.service.application.dto.cmd.ModifyOrderConfigCmd;
import net.hwyz.iov.cloud.otd.vso.service.common.exception.OrderStateNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderAppServiceModifyConfigTest extends BaseTest {

    @Autowired
    private OrderAppService orderAppService;

    @Test
    void testModifyConfigSuccess() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_RED", "OPT_INTERIOR_BLACK");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_001")
                .orderNo("TEST_ORDER_001")
                .optionCodes(optionCodes)
                .build();
        
        orderAppService.modifyConfig(cmd);
    }

    @Test
    void testModifyConfigInvalidState() {
        List<String> optionCodes = Arrays.asList("OPT_COLOR_BLUE");
        
        ModifyOrderConfigCmd cmd = ModifyOrderConfigCmd.builder()
                .accountId("test_user_002")
                .orderNo("TEST_ORDER_LOCKED")
                .optionCodes(optionCodes)
                .build();
        
        assertThrows(OrderStateNotAllowedException.class, () -> {
            orderAppService.modifyConfig(cmd);
        });
    }
}